"""LangGraph 배선. 이 파일만 LangGraph를 import 한다.

    START ─(새 문항)→ generate → validate_question ─(태그·해설 위반)→ generate
      │                              │
      │                              ▼
      │                            dedup ─(중복)→ generate
      │                              │
      │  (기존 문항)                  ▼
      └────────────────────────→ rubric → validate_rubric ─(미달)→ rubric
                                    │
                                    ▼
                                 review  ← interrupt()
                                    │
                                    ▼
                                   END
"""

from __future__ import annotations

import time
from typing import Any

from langgraph.checkpoint.memory import InMemorySaver
from langgraph.graph import END, START, StateGraph
from langgraph.types import interrupt

from core import nodes
from core.state import GraphState, exhausted

DISCARDED_DUPLICATE = "중복을 벗어나지 못했다"
DISCARDED_RUBRIC = "루브릭이 검증을 통과하지 못했다"
DISCARDED_TAGS = "태그가 목록을 벗어났다"
DISCARDED_QUESTION = "문항이 검증을 통과하지 못했다"


def review(state: GraphState) -> GraphState:
    """사람 승인. 여기서 그래프가 멈추고 resume 할 때까지 대기한다."""
    decision = interrupt(
        {
            "question": state["question"],
            "rubric": state["rubric"],
        }
    )
    if isinstance(decision, dict):
        return {
            "verdict": decision.get("verdict", "rejected"),
            "question": decision.get("question", state["question"]),
            "rubric": decision.get("rubric", state["rubric"]),
        }
    return {"verdict": str(decision)}


def _entry(state: GraphState) -> str:
    """문항이 이미 주어졌으면 생성·중복검사를 건너뛴다.

    기존 시드 문항에 루브릭만 붙이는 경로다. 뒤쪽(rubric → validate → review)은
    새 문항과 완전히 같은 것을 쓴다 — 검증 강도를 다르게 둘 이유가 없다.
    """
    return "rubric" if state.get("question") else "generate"


def _after_validate_question(state: GraphState) -> str:
    if not state.get("last_problems"):
        return "dedup"
    return "discard" if exhausted(state.get("generate_attempts", 0)) else "generate"


def _after_dedup(state: GraphState) -> str:
    if not state.get("last_problems"):
        return "rubric"
    return "discard" if exhausted(state.get("generate_attempts", 0)) else "generate"


def _after_validate(state: GraphState) -> str:
    if not state.get("last_problems"):
        return "review"
    return "discard" if exhausted(state.get("rubric_attempts", 0)) else "rubric"


def _discard(state: GraphState) -> GraphState:
    """폐기 사유는 마지막으로 막힌 지점에서 가져온다.

    generate_attempts 로 되짚으면 틀린다 — 중복으로 두 번 다시 만든 뒤 루브릭에서
    막혀도 "중복"으로 보고하게 된다.
    """
    problems = state.get("last_problems", [])
    if any("중복이다" in problem for problem in problems):
        return {"verdict": "discarded", "discarded": DISCARDED_DUPLICATE}
    if any("태그" in problem for problem in problems):
        return {"verdict": "discarded", "discarded": "%s — %s" % (DISCARDED_TAGS, problems[0])}
    if any("정답 해설이" in problem for problem in problems):
        return {"verdict": "discarded", "discarded": "%s — %s" % (DISCARDED_QUESTION, problems[0])}
    if problems:
        return {"verdict": "discarded", "discarded": "%s — %s" % (DISCARDED_RUBRIC, problems[0])}
    return {"verdict": "discarded", "discarded": DISCARDED_RUBRIC}


def _timed(name: str, run):
    """노드 소요 시간을 상태에 누적한다.

    어느 단계가 시간을 먹는지 몰라 개선할 곳을 못 정하고 있었다. 재시도까지 세려면
    노드가 몇 번 불렸는지도 남아야 하므로 덮어쓰지 않고 실행마다 한 줄씩 붙인다.
    """

    def node(state):
        started = time.perf_counter()
        result = run(state)
        entry = {"node": name, "seconds": round(time.perf_counter() - started, 2)}
        return {**result, "timings": list(state.get("timings") or []) + [entry]}

    return node


def build(deps: nodes.Deps, checkpointer: Any | None = None):
    graph = StateGraph(GraphState)

    graph.add_node("generate", _timed("generate", lambda state: nodes.generate(state, deps)))
    graph.add_node("validate_question", _timed("validate_question", lambda state: nodes.validate_question(state, deps)))
    graph.add_node("dedup", _timed("dedup", lambda state: nodes.dedup(state, deps)))
    graph.add_node("rubric", _timed("rubric", lambda state: nodes.make_rubric(state, deps)))
    graph.add_node("validate_rubric", _timed("validate_rubric", lambda state: nodes.validate_rubric(state, deps)))
    graph.add_node("review", review)
    graph.add_node("discard", _discard)

    graph.add_conditional_edges(START, _entry, {"generate": "generate", "rubric": "rubric"})
    graph.add_edge("generate", "validate_question")
    graph.add_conditional_edges(
        "validate_question",
        _after_validate_question,
        {"dedup": "dedup", "generate": "generate", "discard": "discard"},
    )
    graph.add_conditional_edges(
        "dedup", _after_dedup, {"rubric": "rubric", "generate": "generate", "discard": "discard"}
    )
    graph.add_edge("rubric", "validate_rubric")
    graph.add_conditional_edges(
        "validate_rubric",
        _after_validate,
        {"review": "review", "rubric": "rubric", "discard": "discard"},
    )
    graph.add_edge("review", END)
    graph.add_edge("discard", END)

    return graph.compile(checkpointer=checkpointer or InMemorySaver())
