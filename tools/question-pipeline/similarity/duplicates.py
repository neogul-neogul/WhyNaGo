"""서술형 문항 사이의 중복 후보를 찾는다.

임베딩을 쓰지 않는 1차 통과다. 이 단계의 산출물은 "중복 판정"이 아니라
**사람이 볼 후보 목록**이고, 그 목록에 사람이 표시를 하면 유사도 임계값이 데이터로 정해진다.
임계값을 감으로 정하면 3단계 게이트가 무의미해지므로 이게 선행돼야 한다.

    python tools/question-pipeline/duplicates.py --top 40
"""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import dataclass
from itertools import combinations
from pathlib import Path

from adapters import seed

NGRAM_SIZE = 3
DEFAULT_TOP = 40
NON_WORD = re.compile(r"[^0-9A-Za-z가-힣]+")
PROBES_PATH = Path(__file__).resolve().parent / "probes.json"


@dataclass(frozen=True)
class Pair:
    left: seed.Question
    right: seed.Question
    text_similarity: float
    tag_similarity: float
    shared_tags: tuple[str, ...]

    @property
    def score(self) -> float:
        return max(self.text_similarity, self.tag_similarity)


def ngrams(text: str, size: int = NGRAM_SIZE) -> set[str]:
    normalized = NON_WORD.sub("", text)
    if len(normalized) < size:
        return {normalized} if normalized else set()
    return {normalized[index : index + size] for index in range(len(normalized) - size + 1)}


def jaccard(left: set[str], right: set[str]) -> float:
    """두 텍스트가 서로 얼마나 겹치나. 길이가 비슷할 때만 의미가 있다."""
    if not left or not right:
        return 0.0
    return len(left & right) / len(left | right)


def containment(part: set[str], whole: set[str]) -> float:
    """짧은 쪽이 긴 쪽 안에 얼마나 들어 있나.

    길이 차가 크면 Jaccard를 쓰면 안 된다. 40자 항목과 250자 해설은
    완전히 일치해도 Jaccard 상한이 0.16이라 어떤 임계값을 잡아도 전부 탈락한다.
    """
    if not part or not whole:
        return 0.0
    return len(part & whole) / len(part)


def pairs(questions: list[seed.Question]) -> list[Pair]:
    """같은 카테고리 안에서만 비교한다. 카테고리가 다르면 중복일 수 없다."""
    grams = {question.variable: ngrams(question.text) for question in questions}
    found: list[Pair] = []
    for category in sorted({question.category for question in questions}):
        group = [question for question in questions if question.category == category]
        for left, right in combinations(group, 2):
            left_tags, right_tags = set(left.tags), set(right.tags)
            found.append(
                Pair(
                    left=left,
                    right=right,
                    text_similarity=jaccard(grams[left.variable], grams[right.variable]),
                    tag_similarity=jaccard(left_tags, right_tags),
                    shared_tags=tuple(sorted(left_tags & right_tags)),
                )
            )
    return sorted(found, key=lambda pair: pair.score, reverse=True)


def render(questions: list[seed.Question], found: list[Pair], top: int) -> str:
    lines = [
        f"서술형 중복 후보   문항={len(questions)}   비교쌍={len(found)}   상위={top}",
        "지표: 제목+발문 문자 3-gram Jaccard(TEXT), 태그 Jaccard(TAG)",
        "",
        f"{'#':>3}  {'TEXT':>5}  {'TAG':>5}  {'CAT':<10}  문항 / 겹치는 태그",
    ]
    for index, pair in enumerate(found[:top], start=1):
        lines.append(
            f"{index:>3}  {pair.text_similarity:>5.3f}  {pair.tag_similarity:>5.3f}  "
            f"{pair.left.category:<10}  {pair.left.title}"
        )
        lines.append(f"{'':>3}  {'':>5}  {'':>5}  {'':<10}  {pair.right.title}")
        if pair.shared_tags:
            lines.append(f"{'':>3}  {'':>5}  {'':>5}  {'':<10}  ↳ {', '.join(pair.shared_tags)}")
        lines.append("")

    lines.append(_distribution(found))
    return "\n".join(lines)


def _distribution(found: list[Pair]) -> str:
    buckets = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
    lines = ["TEXT 유사도 분포 (임계값 후보를 눈으로 잡기 위한 것)"]
    for floor in reversed(buckets):
        count = sum(1 for pair in found if floor <= pair.text_similarity < floor + 0.1)
        if count:
            lines.append(f"  {floor:.1f}~{floor + 0.1:.1f}  {count:>6}  {'#' * min(count // 20 + 1, 40)}")
    return "\n".join(lines)


def calibrate(questions: list[seed.Question], found: list[Pair]) -> str:
    """알려진 중복·비중복 탐침이 어디에 떨어지는지 보고 임계값을 잡는다.

    시드에 중복이 없으면 분포만으로는 컷을 정할 수 없다. 높은 쪽 앵커가 없기 때문이다.
    사람이 만든 중복을 넣어 보면 "중복이면 최소 이만큼은 나온다"는 하한이 생긴다.
    """
    probes = json.loads(PROBES_PATH.read_text(encoding="utf-8"))["probes"]
    by_title = {question.title: question for question in questions}
    baseline = max((pair.text_similarity for pair in found), default=0.0)

    lines = [
        "",
        "임계값 눈금 (probes.json)",
        f"  시드 안 최댓값(중복 없음으로 간주) = {baseline:.3f}",
        "",
        f"  {'ID':<4} {'KIND':<10} {'원문 대비':>9} {'카테고리 내 최고':>16}  문항",
    ]
    for probe in probes:
        source = by_title.get(probe["sourceTitle"])
        if source is None:
            raise ValueError(f"탐침의 원본 문항을 찾을 수 없다 - title={probe['sourceTitle']}")
        probe_grams = ngrams(f"{probe['title']}\n{probe['content']}")
        against_source = jaccard(probe_grams, ngrams(source.text))
        nearest_score, nearest_title = max(
            (
                (jaccard(probe_grams, ngrams(other.text)), other.title)
                for other in questions
                if other.category == source.category and other.title != source.title
            ),
            default=(0.0, ""),
        )
        lines.append(
            f"  {probe['id']:<4} {probe['kind']:<10} {against_source:>9.3f} "
            f"{nearest_score:>16.3f}  {probe['title']}"
        )
        lines.append(f"  {'':<4} {'':<10} {'':>9} {'':>16}  ↳ 최근접: {nearest_title}")

    def scores_of(kind: str) -> list[float]:
        return [
            jaccard(
                ngrams(f"{probe['title']}\n{probe['content']}"),
                ngrams(by_title[probe["sourceTitle"]].text),
            )
            for probe in probes
            if probe["kind"] == kind
        ]

    lexical = scores_of("lexical")
    paraphrase = scores_of("paraphrase")
    lines += [
        "",
        f"  어휘 중복(lexical)   최저 {min(lexical):.3f}",
        f"  시드 안 최댓값        {baseline:.3f}   (비중복 상한)",
        f"  의미 중복(paraphrase) 최고 {max(paraphrase):.3f}",
        "",
    ]
    if min(lexical) > baseline:
        lines.append(f"  → 어휘 중복 컷: {(baseline + min(lexical)) / 2:.3f} — 이 위는 사람이 볼 가치가 있다")
    else:
        lines.append("  ⚠ 어휘 중복조차 비중복과 안 갈린다 — 이 지표를 버려야 한다")
    if max(paraphrase) <= baseline:
        lines.append(
            f"  ⚠ 의미 중복은 비중복({baseline:.3f})보다 낮게 나온다 — 어휘 유사도로는 원리적으로 못 잡는다"
        )
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="서술형 문항 중복 후보 리포트")
    parser.add_argument("--top", type=int, default=DEFAULT_TOP, help="출력할 상위 쌍 수")
    parser.add_argument("--out", type=Path, help="리포트를 파일로도 저장할 경로")
    parser.add_argument("--calibrate", action="store_true", help="탐침으로 임계값 눈금을 잡는다")
    arguments = parser.parse_args()

    questions = seed.essays(seed.load())
    found = pairs(questions)
    report = render(questions, found, arguments.top)
    if arguments.calibrate:
        report += "\n" + calibrate(questions, found)

    print(report)
    if arguments.out:
        arguments.out.write_text(report, encoding="utf-8")


if __name__ == "__main__":
    main()
