"""로컬 임베딩(Ollama)으로 의미 중복을 잰다.

어휘 유사도가 못 잡는 구간이 있다는 건 2-A에서 측정으로 나왔다(`docs/RUBRIC_PIPELINE.md`).
이 파일은 그 구간을 임베딩이 실제로 메우는지 **같은 탐침으로 판정**한다.
못 메우면 채택하지 않는다.

    uv run embeddings.py --calibrate
"""

from __future__ import annotations

import argparse
import json
import math
import urllib.error
import urllib.request
from pathlib import Path
from typing import Iterable

from similarity import duplicates
from adapters import seed

HOST = "http://localhost:11434"
MODEL = "bge-m3"
BATCH = 32
TIMEOUT = 300

CACHE_PATH = Path(__file__).resolve().parents[1] / ".work" / "embeddings.json"
PROBES_PATH = Path(__file__).resolve().parent / "probes.json"


class EmbedError(RuntimeError):
    pass


def embed(texts: list[str], model: str = MODEL) -> list[list[float]]:
    vectors: list[list[float]] = []
    for start in range(0, len(texts), BATCH):
        vectors += _call(texts[start : start + BATCH], model)
    return vectors


def _call(batch: list[str], model: str) -> list[list[float]]:
    request = urllib.request.Request(
        "%s/api/embed" % HOST,
        data=json.dumps({"model": model, "input": batch}).encode("utf-8"),
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT) as response:
            return json.loads(response.read())["embeddings"]
    except urllib.error.URLError as error:
        raise EmbedError(
            "Ollama에 붙지 못했다 - %s. `ollama list`로 데몬과 %s 모델을 확인하라." % (error, model)
        ) from error


class Cache:
    """텍스트 → 벡터. 문항이 안 바뀌면 다시 부르지 않는다."""

    def __init__(self, path: Path = CACHE_PATH) -> None:
        self.path = path
        self.entries: dict[str, list[float]] = {}
        if path.exists():
            self.entries = json.loads(path.read_text(encoding="utf-8"))

    def resolve(self, texts: Iterable[str]) -> dict[str, list[float]]:
        missing = sorted({text for text in texts if text not in self.entries})
        if missing:
            for text, vector in zip(missing, embed(missing)):
                self.entries[text] = vector
            self.save()
        return {text: self.entries[text] for text in texts}

    def save(self) -> None:
        self.path.parent.mkdir(exist_ok=True)
        self.path.write_text(json.dumps(self.entries), encoding="utf-8")


def cosine(left: list[float], right: list[float]) -> float:
    dot = sum(a * b for a, b in zip(left, right))
    norm = math.sqrt(sum(a * a for a in left)) * math.sqrt(sum(b * b for b in right))
    return dot / norm if norm else 0.0


def nearest(query: str, candidates: list[tuple[str, str]]) -> tuple[str, float]:
    """질의와 가장 가까운 후보와 그 유사도.

    중복 게이트의 판정 근거다. 어휘 유사도가 원리적으로 못 잡는 구간을 메운다 —
    같은 문항인데 어휘로는 0.225, 임베딩으로는 0.936으로 나온 기록이 evidence 에 있다.
    """
    if not candidates:
        return ("", 0.0)
    vectors = Cache().resolve([text for _, text in candidates] + [query])
    scored = [(cosine(vectors[query], vectors[text]), title) for title, text in candidates]
    score, title = max(scored)
    return (title, score)


def retriever(query: str, candidates: list[tuple[str, str]], top: int) -> list[str]:
    """질의에 의미적으로 가까운 후보 제목을 top개 고른다.

    생성 프롬프트의 '이미 있는 주제' 목록을 좁히는 데 쓴다. 카테고리 전체를 넣으면
    DB만 28개, NETWORK는 40개가 넘어 정작 겹칠 문항이 그 안에 묻힌다.
    태그 이름이 아니라 **문항 본문**으로 재야 한다 — 같은 태그끼리는 태그로 구분되지 않는다.
    """
    if len(candidates) <= top:
        return [title for title, _ in candidates]
    vectors = Cache().resolve([text for _, text in candidates] + [query])
    scored = sorted(
        ((cosine(vectors[query], vectors[text]), title) for title, text in candidates),
        reverse=True,
    )
    return [title for _, title in scored[:top]]


def calibrate() -> str:
    questions = seed.essays(seed.load())
    probes = json.loads(PROBES_PATH.read_text(encoding="utf-8"))["probes"]
    by_title = {question.title: question for question in questions}

    probe_texts = ["%s\n%s" % (probe["title"], probe["content"]) for probe in probes]
    cache = Cache()
    vectors = cache.resolve([question.text for question in questions] + probe_texts)

    lines = ["# 임베딩 눈금 (%s)" % MODEL, ""]
    lines.append("%-4s %-12s %-38.38s %8s %8s" % ("id", "유형", "선언된 원문", "원문", "최근접"))
    for probe, text in zip(probes, probe_texts):
        source = by_title.get(probe["sourceTitle"])
        to_source = cosine(vectors[text], vectors[source.text]) if source else float("nan")
        nearest, score = _nearest(vectors[text], questions, vectors)
        lines.append(
            "%-4s %-12s %-38.38s %8.3f %8.3f  → %s"
            % (probe["id"], probe["kind"], probe["sourceTitle"], to_source, score, nearest)
        )

    ceiling, pair = _unrelated_ceiling(questions, vectors)
    lines += ["", "시드 쌍 유사도 p99: %.3f  (%s ↔ %s)" % (ceiling, pair[0], pair[1])]
    lines += ["", _verdict(probes, probe_texts, by_title, vectors, ceiling)]
    return "\n".join(lines)


def _nearest(vector: list[float], questions, vectors) -> tuple[str, float]:
    scored = [(question.title, cosine(vector, vectors[question.text])) for question in questions]
    return max(scored, key=lambda pair: pair[1], default=("", 0.0))


def _unrelated_ceiling(questions, vectors) -> tuple[float, tuple[str, str]]:
    """시드 쌍 유사도의 p99. 의미 중복은 이 위로 올라와야 한다.

    최댓값을 쓰면 안 된다. 시드에 실재하는 중복(제목까지 같은 쌍이 있다)이 잡혀서
    "무관한 쌍의 상한"이 0.974로 나오고, 그걸 기준 삼으면 무엇도 통과하지 못한다.
    """
    scored = sorted(
        (
            (cosine(vectors[left.text], vectors[right.text]), left.title, right.title)
            for index, left in enumerate(questions)
            for right in questions[index + 1 :]
        ),
        reverse=True,
    )
    if not scored:
        return 0.0, ("", "")
    percentile = scored[max(int(len(scored) * 0.01) - 1, 0)]
    return percentile[0], (percentile[1], percentile[2])


def _verdict(probes, probe_texts, by_title, vectors, ceiling: float) -> str:
    paraphrase = []
    distinct = []
    for probe, text in zip(probes, probe_texts):
        source = by_title.get(probe["sourceTitle"])
        if not source:
            continue
        score = cosine(vectors[text], vectors[source.text])
        if probe["kind"] == "paraphrase":
            paraphrase.append((probe["id"], score))
        elif probe["kind"] == "distinct":
            distinct.append((probe["id"], score))

    lowest = min((score for _, score in paraphrase), default=0.0)
    highest = max((score for _, score in distinct), default=0.0)
    floor = max(highest, ceiling)
    lines = [
        "의미 중복 최저: %.3f   대조군 최고: %.3f   시드 p99: %.3f" % (lowest, highest, ceiling),
    ]
    if lowest > floor:
        lines.append("판정: 채택 가능. 컷은 %.3f ~ %.3f 사이." % (floor, lowest))
    else:
        lines.append("판정: 이 모델로는 의미 중복과 대조군이 갈리지 않는다. 게이트로 쓰면 안 된다.")
    return "\n".join(lines)


def compare_lexical() -> str:
    """같은 탐침을 어휘 유사도로도 재서 나란히 놓는다."""
    questions = seed.essays(seed.load())
    probes = json.loads(PROBES_PATH.read_text(encoding="utf-8"))["probes"]
    by_title = {question.title: question for question in questions}
    lines = ["", "# 같은 탐침의 어휘 유사도 (대조)", ""]
    for probe in probes:
        source = by_title.get(probe["sourceTitle"])
        if not source:
            continue
        grams = duplicates.ngrams("%s\n%s" % (probe["title"], probe["content"]))
        lines.append(
            "%-4s %-12s %.3f" % (probe["id"], probe["kind"], duplicates.jaccard(grams, duplicates.ngrams(source.text)))
        )
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="로컬 임베딩 기반 의미 중복 측정")
    parser.add_argument("--calibrate", action="store_true", help="탐침으로 채택 여부를 판정한다")
    parser.add_argument("--lexical", action="store_true", help="어휘 유사도도 함께 출력한다")
    arguments = parser.parse_args()

    if not arguments.calibrate:
        parser.error("지금은 --calibrate 만 있다")
    report = calibrate()
    if arguments.lexical:
        report += "\n" + compare_lexical()
    print(report)


if __name__ == "__main__":
    main()
