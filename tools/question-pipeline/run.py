"""드라이버. 그래프 바깥에서 태그·문항을 순회한다.

흐름은 태그 → 문제 생성 → 루브릭 생성 → 문제 컨펌 → 문제 출제다.
승인을 문항마다 물으면 사람이 붙잡혀 있어야 하므로, 전부 interrupt 지점까지 파킹해
리뷰 파일 하나로 덤프하고, 사람이 일괄 수정한 뒤 일괄 resume 한다.

    uv run run.py tags --category DB          # 태그 사전 확인
    uv run run.py generate --tag 인덱스 --count 3 --dry-run
    uv run run.py generate --category DB --count 5 --dry-run   # 사전 순서대로 태그 자동 선택
    uv run run.py review                      # 리뷰 파일을 눈으로 확인
    uv run run.py resume --out ../../src/main/resources/data5-generated.sql

기존 시드 문항에 루브릭만 붙이는 경로도 같은 흐름이다. 산출물이 INSERT가 아니라 UPDATE다.

    uv run run.py backfill --category DB --limit 3 --dry-run
    uv run run.py review
    uv run run.py resume --out ../../src/main/resources/data5-rubric.sql
"""

from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime
from pathlib import Path
from typing import Any

from langgraph.checkpoint.sqlite import SqliteSaver
from langgraph.types import Command

from testing import fakes
from core import graph as graph_module
from core import nodes
from core import prompts as prompts_module
from adapters import seed
from adapters import sql
from core import state as state_module
from similarity import embeddings
from vocabulary import tags as tags_module
from adapters.llm import GeminiClient, OllamaClient, UnusedClient

WORK_DIR = Path(__file__).resolve().parent / ".work"
CHECKPOINT_DB = WORK_DIR / "checkpoints.sqlite"
REVIEW_PATH = WORK_DIR / "review.json"

NEW_QUESTION = "new"
EXISTING_QUESTION = "existing"

# 기본값은 pending 이다. 사람이 손대지 않은 문항이 resume 한 번으로 전부 시드에 들어가면
# 승인 단계가 있으나 마나다. 결정이 찍힌 것만 재개한다.
PENDING = "pending"
DECIDED = ("approved", "rejected")


def _client(arguments):
    """기본은 로컬(Ollama)이다. --gemini 를 줘야 외부 API를 쓴다."""
    if arguments.dry_run:
        return fakes.scripted_client()
    if arguments.gemini:
        return GeminiClient(arguments.api_key)
    return OllamaClient(arguments.model)


def _retriever(enabled: bool):
    """'이미 있는 주제'를 질의에 가까운 것만 남긴다. --no-rag 면 카테고리 전체를 넣는다.

    기본값이다. 껐을 때 OS 배치에서 10개 중 4개가 중복으로 폐기됐는데, 같은 프롬프트로
    켜고 다시 돌리니 4개 태그가 전부 통과했다(PCB 0.966 -> 0.809).
    카테고리가 50개를 넘어가면 정작 겹칠 문항이 목록에 묻힌다.
    근거는 evidence/2026-08-20-rag-retry.md 에 있다.
    """
    return embeddings.retriever if enabled else None


def _semantic(enabled: bool):
    """--no-semantic 이 아니면 중복 게이트 2단계(의미 유사도)를 켠다."""
    return None if enabled else embeddings.nearest


def _conditions(deps: nodes.Deps) -> str:
    """실행 조건을 한 줄로. evidence 기록만 보고 어떤 설정이었는지 복원돼야 한다.

    프롬프트 버전만 찍던 때에 RAG 를 켜고 잰 결과와 끄고 잰 결과를 섞어 비교할 뻔했다.
    """
    gate = "컷 %.2f" % deps.cut if deps.semantic else "중복 게이트 꺼짐"
    rag = "RAG 켬" if deps.retriever else "RAG 끔"
    return "프롬프트 %s · %s · %s" % (deps.prompts.stamp, rag, gate)


def _thread(tag: str, index: int) -> dict[str, Any]:
    return {"configurable": {"thread_id": "%s-%d" % (tag, index)}}


def _planned_tags(arguments: argparse.Namespace) -> list[tags_module.Tag]:
    """--tag 면 그 태그만, --category 면 사전 순서대로 고른다.

    같은 태그를 몇 번 요청하든 자르지 않는다. 태그 안에서 몇 개까지 갈리는지는
    RAG 주입과 유사도 게이트가 판정한다.
    """
    if not arguments.tag:
        if arguments.per_tag > 1:
            return tags_module.cluster_plan(
                arguments.count, category=arguments.category, per_tag=arguments.per_tag
            )
        return tags_module.plan(arguments.count, category=arguments.category)

    return [tags_module.find(arguments.tag)] * arguments.count


def command_tags(arguments: argparse.Namespace) -> None:
    catalog = (
        tags_module.by_category(arguments.category) if arguments.category else list(tags_module.load())
    )
    current = ""
    for tag in catalog:
        if tag.category != current:
            current = tag.category
            print("\n[%s]" % current)
        print("  %s" % tag.name)


def command_generate(arguments: argparse.Namespace) -> None:
    WORK_DIR.mkdir(exist_ok=True)
    planned = _planned_tags(arguments)
    catalog = seed.essays(seed.load())
    deps = nodes.Deps.create(
        _client(arguments),
        existing=catalog,
        retriever=_retriever(not arguments.no_rag),
        semantic=_semantic(arguments.no_semantic),
        prompt_version=arguments.prompt,
    )
    print("%s\n" % _conditions(deps))

    with SqliteSaver.from_conn_string(str(CHECKPOINT_DB)) as checkpointer:
        compiled = graph_module.build(deps, checkpointer)
        parked: list[dict[str, Any]] = []

        for index, tag in enumerate(planned, start=1):
            config = _thread(tag.name, index)
            thread_id = config["configurable"]["thread_id"]
            try:
                state = compiled.invoke(
                    state_module.initial(
                        category=tag.category, difficulty=arguments.difficulty, tag=tag.name
                    ),
                    config,
                )
            except Exception as error:
                # 한 문항이 죽어도 나머지와 앞서 만든 결과를 잃지 않는다.
                entry = {"thread_id": thread_id, "status": "failed", "error": repr(error)}
            else:
                entry = _parked_entry(config, state)
                _remember(deps, entry)
            parked.append(entry)
            print("%-28.28s %s%s" % (thread_id, _summary(entry), _elapsed(entry)), flush=True)

    _write_review(parked)


def _remember(deps: nodes.Deps, entry: dict[str, Any]) -> None:
    """이번 실행에서 만든 문항을 다음 문항의 기준선에 넣는다.

    **폐기된 것도 넣는다.** 승인 대기까지 간 것만 넣으면, 폐기 직후의 문항이 그 존재를 모른 채
    같은 주제를 다시 만든다. 실제로 NETWORK 배치에서 컷을 넘은 두 쌍(0.918, 0.574)의
    앞 문항이 모두 폐기된 것이었다. 게이트가 답을 갖고 있었는데 물어보질 않았다.
    """
    if entry.get("question"):
        deps.produced.append(entry["question"])


def command_backfill(arguments: argparse.Namespace) -> None:
    """이미 있는 문항에 루브릭만 붙인다. 문항 자체는 만들지도 고치지도 않는다."""
    WORK_DIR.mkdir(exist_ok=True)
    catalog = seed.load()
    targets = seed.needs_rubric(catalog)
    if arguments.category:
        targets = [target for target in targets if target.category == arguments.category]
    skipped = seed.ambiguous_titles(catalog)
    if skipped:
        print("제목이 겹쳐 제외한 문항: %s" % ", ".join(skipped))
    print("루브릭 없는 서술형 %d개 중 %d개를 처리한다.\n" % (len(targets), min(len(targets), arguments.limit)))
    targets = targets[: arguments.limit]

    deps = nodes.Deps.create(
        _client(arguments), existing=seed.essays(catalog), prompt_version=arguments.prompt
    )
    print("%s\n" % _conditions(deps))
    parked: list[dict[str, Any]] = []

    with SqliteSaver.from_conn_string(str(CHECKPOINT_DB)) as checkpointer:
        compiled = graph_module.build(deps, checkpointer)
        for target in targets:
            config = {"configurable": {"thread_id": "backfill-%s" % target.title}}
            try:
                state = compiled.invoke(_backfill_input(target), config)
            except Exception as error:
                entry = {
                    "thread_id": config["configurable"]["thread_id"],
                    "mode": EXISTING_QUESTION,
                    "status": "failed",
                    "error": repr(error),
                }
            else:
                entry = _parked_entry(config, state, EXISTING_QUESTION)
            parked.append(entry)
            print("%-40.40s %s%s" % (target.title, _summary(entry), _elapsed(entry)), flush=True)

    _write_review(parked)


def _backfill_input(target: seed.Question) -> dict[str, Any]:
    return state_module.initial(
        category=target.category,
        difficulty=target.difficulty,
        existing=True,
        question={
            "title": target.title,
            "content": target.content,
            "explanation": target.explanation,
            "category": target.category,
            "difficulty": target.difficulty,
            "tags": list(target.tags),
        },
    )


def decided(entry: dict[str, Any]) -> bool:
    """사람이 결정을 찍었는가.

    pending 인 채로 resume 하면 안 된다. interrupt 를 소모해 버려서 나중에 승인으로
    바꿔도 그 문항은 다시 재개할 수 없다. 그래서 아예 건너뛰고 파킹 상태를 유지한다.
    """
    return entry.get("verdict") in DECIDED


def _backup_undecided() -> None:
    """결정이 안 끝난 리뷰 파일은 덮어쓰기 전에 옆으로 옮긴다.

    generate 는 리뷰 파일을 통째로 갈아엎는다. dry-run 한 번에 검수 대기 중이던
    배치를 날린 적이 두 번 있다. 체크포인트에서 복구는 되지만 알아채기 전까지가 문제다.
    """
    if not REVIEW_PATH.exists():
        return
    try:
        entries = json.loads(REVIEW_PATH.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError):
        entries = []
    undecided = [
        entry for entry in entries
        if entry.get("status") == "awaiting_review" and not decided(entry)
    ]
    if not undecided:
        return
    backup = REVIEW_PATH.with_name("review-%s.json" % datetime.now().strftime("%Y%m%d-%H%M%S"))
    REVIEW_PATH.replace(backup)
    print("검수 대기 %d건이 있어 %s 로 옮겼다." % (len(undecided), backup.name))


def _write_review(parked: list[dict[str, Any]]) -> None:
    _backup_undecided()
    REVIEW_PATH.write_text(json.dumps(parked, ensure_ascii=False, indent=2), encoding="utf-8")
    print("\n리뷰 파일 %s" % REVIEW_PATH)
    print("verdict 가 %s 다. approved/rejected 로 바꾸고 필요하면 question·rubric 을 "
          "직접 고친 뒤 resume 하라. %s 인 문항은 재개하지 않는다." % (PENDING, PENDING))


def _parked_entry(
    config: dict[str, Any], state: dict[str, Any], mode: str = NEW_QUESTION
) -> dict[str, Any]:
    if state.get("verdict") == "discarded":
        return {
            "thread_id": config["configurable"]["thread_id"],
            "mode": mode,
            "status": "discarded",
            "reason": state.get("discarded", ""),
            "failures": state.get("generate_failures", []) + state.get("rubric_failures", []),
            "question": state.get("question"),
            "rubric": state.get("rubric"),
            "timings": state.get("timings", []),
        }
    return {
        "thread_id": config["configurable"]["thread_id"],
        "mode": mode,
        "status": "awaiting_review",
        "verdict": PENDING,
        "question": state["question"],
        "rubric": state["rubric"],
        "timings": state.get("timings", []),
    }


def _elapsed(entry: dict[str, Any]) -> str:
    """노드별 소요를 한 줄로. 재시도로 같은 노드가 여러 번 불리면 합치고 횟수를 붙인다."""
    timings = entry.get("timings") or []
    if not timings:
        return ""
    spent: dict[str, list[float]] = {}
    for item in timings:
        spent.setdefault(item["node"], []).append(item["seconds"])
    parts = [
        "%s %.0fs%s" % (node, sum(seconds), "x%d" % len(seconds) if len(seconds) > 1 else "")
        for node, seconds in sorted(spent.items(), key=lambda pair: -sum(pair[1]))
    ]
    return "  [%.0fs = %s]" % (sum(item["seconds"] for item in timings), ", ".join(parts))


def _summary(entry: dict[str, Any]) -> str:
    if entry["status"] == "failed":
        return "실패 — %s" % entry["error"]
    if entry["status"] == "discarded":
        return "폐기 — %s" % entry["reason"]
    return "승인 대기 — %s" % entry["question"]["title"]


def command_review(_: argparse.Namespace) -> None:
    for entry in _load_review():
        print("=" * 78)
        verdict = "  verdict=%s" % entry["verdict"] if entry.get("verdict") else ""
        print("%s  %s%s" % (entry["thread_id"], entry["status"], verdict))
        if entry["status"] == "failed":
            print("  오류: %s" % entry["error"])
            continue
        if entry["status"] == "discarded":
            print("  사유: %s" % entry["reason"])
            for failure in entry["failures"]:
                print("   - %s" % failure)
            continue
        question = entry["question"]
        print("  제목: %s" % question["title"])
        print("  발문: %s" % question["content"])
        print("  해설: %s" % question["explanation"])
        print("  태그: %s" % ", ".join(question.get("tags", [])))
        print("  프롬프트: %s" % question.get(nodes.PROMPT_KEY, "-"))
        for item in entry["rubric"]["criteria"]:
            print("   (%s점) %s" % (item["weight"], item["point"]))


def command_resume(arguments: argparse.Namespace) -> None:
    reviewed = _load_review()
    deps = nodes.Deps.create(UnusedClient())

    approved: list[dict[str, Any]] = []
    pending: list[str] = []
    with SqliteSaver.from_conn_string(str(CHECKPOINT_DB)) as checkpointer:
        compiled = graph_module.build(deps, checkpointer)
        for entry in reviewed:
            if entry["status"] != "awaiting_review":
                continue
            if not decided(entry):
                pending.append(entry["thread_id"])
                continue
            config = {"configurable": {"thread_id": entry["thread_id"]}}
            state = compiled.invoke(
                Command(
                    resume={
                        "verdict": entry["verdict"],
                        "question": entry["question"],
                        "rubric": entry["rubric"],
                    }
                ),
                config,
            )
            if state.get("verdict") == "approved":
                approved.append({**state["question"], "rubric": state["rubric"]})
            print("%-14s %s" % (entry["thread_id"], state.get("verdict")))

    if pending:
        print("\n%d개는 %s 라서 건너뛴다 - %s" % (len(pending), PENDING, ", ".join(pending)))
        print("결정한 뒤 다시 resume 하면 이어서 재개한다.")
    if not approved:
        print("\n승인된 문항이 없다. SQL을 만들지 않는다.")
        return
    rendered = _render(reviewed, approved)
    if arguments.out:
        Path(arguments.out).write_text(rendered, encoding="utf-8")
        print("\n%d개 문항 → %s" % (len(approved), arguments.out))
    else:
        print()
        print(rendered)


def _render(reviewed: list[dict[str, Any]], approved: list[dict[str, Any]]) -> str:
    """새 문항이면 INSERT, 기존 문항이면 rubric UPDATE만.

    두 모드가 섞이면 산출 파일 하나에 넣을 수 없다. generate·backfill이 리뷰 파일을
    통째로 덮어쓰므로 실제로 섞이지 않지만, 섞였다면 조용히 한쪽을 버리는 대신 멈춘다.
    """
    modes = {entry.get("mode", NEW_QUESTION) for entry in reviewed}
    if len(modes) > 1:
        raise SystemExit("리뷰 파일에 새 문항과 기존 문항이 섞여 있다 - modes=%s" % sorted(modes))
    if modes == {EXISTING_QUESTION}:
        return sql.render_rubrics(approved)
    return sql.render(approved)


def _load_review() -> list[dict[str, Any]]:
    if not REVIEW_PATH.exists():
        raise SystemExit("리뷰 파일이 없다. 먼저 generate 를 돌려라 - %s" % REVIEW_PATH)
    return json.loads(REVIEW_PATH.read_text(encoding="utf-8"))


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description="문항·루브릭 생성 파이프라인")
    subparsers = parser.add_subparsers(dest="command", required=True)

    generate = subparsers.add_parser("generate", help="interrupt 지점까지 돌려 리뷰 파일을 만든다")
    axis = generate.add_mutually_exclusive_group(required=True)
    axis.add_argument("--tag", help="이 태그로만 만든다")
    axis.add_argument("--category", help="카테고리 안에서 사전 순서대로 태그를 고른다")
    generate.add_argument("--difficulty", default="MEDIUM", choices=["LOW", "MEDIUM", "HIGH"])
    generate.add_argument("--count", type=int, default=1)
    generate.add_argument(
        "--per-tag",
        type=int,
        default=1,
        help="--category 일 때 한 태그에 몰아 줄 최대 문항 수 (기본 1 = 태그마다 하나씩)",
    )
    generate.add_argument(
        "--no-semantic",
        action="store_true",
        help="중복 게이트에서 의미 유사도 단계를 끈다 (어휘만 본다)",
    )
    generate.add_argument(
        "--no-rag",
        action="store_true",
        help="'이미 있는 주제'를 좁히지 않고 카테고리 전체를 넣는다",
    )
    generate.set_defaults(handler=command_generate)

    tag_list = subparsers.add_parser("tags", help="태그 어휘를 출력한다")
    tag_list.add_argument("--category", help="없으면 전체")
    tag_list.set_defaults(handler=command_tags)

    backfill = subparsers.add_parser("backfill", help="기존 시드 문항에 루브릭만 붙인다")
    backfill.add_argument("--category", help="없으면 전체 카테고리")
    backfill.add_argument("--limit", type=int, default=1)
    backfill.set_defaults(handler=command_backfill)

    review = subparsers.add_parser("review", help="리뷰 파일을 사람이 읽기 좋게 출력한다")
    review.set_defaults(handler=command_review)

    resume = subparsers.add_parser("resume", help="승인 결과로 재개하고 SQL을 뽑는다")
    resume.add_argument("--out", help="SQL을 저장할 경로")
    resume.set_defaults(handler=command_resume)

    for subparser in (generate, backfill, review, resume, tag_list):
        subparser.add_argument("--dry-run", action="store_true", help="LLM 없이 대본으로 돈다")
        subparser.add_argument("--gemini", action="store_true", help="로컬 대신 Gemini API를 쓴다")
        subparser.add_argument("--model", default=None, help="Ollama 모델 (기본 qwen3:8b)")
        subparser.add_argument("--api-key", help="--gemini 일 때. 없으면 환경변수 API_KEY")
        subparser.add_argument(
            "--prompt",
            choices=prompts_module.versions(),
            help="프롬프트 버전 (기본 %s). 버전을 비교할 때만 쓴다" % prompts_module.CURRENT_VERSION,
        )

    arguments = parser.parse_args()
    arguments.handler(arguments)


if __name__ == "__main__":
    main()
