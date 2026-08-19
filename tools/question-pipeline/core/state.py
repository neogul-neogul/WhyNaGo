"""그래프 상태. LangGraph를 import 하지 않는다.

실패 목록을 `Annotated[list, operator.add]`로 두지 않는다. 리듀서로는 비울 수가 없어서
문항 생성 실패 사유가 루브릭 재시도 프롬프트까지 따라 들어간다. 단계별로 나누고 직접 누적한다.
"""

from __future__ import annotations

from typing import Any, TypedDict

MAX_ATTEMPTS = 3


class GraphState(TypedDict, total=False):
    category: str
    difficulty: str
    tag: str

    question: dict[str, Any] | None
    rubric: dict[str, Any] | None
    existing: bool

    generate_attempts: int
    rubric_attempts: int

    generate_failures: list[str]
    rubric_failures: list[str]
    last_problems: list[str]

    # [{'node': ..., 'seconds': ...}]. 리듀서를 쓰지 않고 노드가 직접 누적한다 —
    # 리듀서로 두면 같은 thread_id 로 다시 돌릴 때 지난 실행 값이 남는다.
    timings: list
    verdict: str
    discarded: str


def exhausted(attempts: int) -> bool:
    return attempts >= MAX_ATTEMPTS


def initial(**overrides: Any) -> GraphState:
    """모든 필드를 명시해 초기화한다.

    같은 thread_id로 다시 돌리면 LangGraph가 입력을 **기존 체크포인트에 병합**한다.
    빠뜨린 필드는 지난 실행 값이 그대로 남아, 이미 해결된 실패 사유가 새 실행을 폐기시킨다.
    실제로 그렇게 물렸다.
    """
    state: GraphState = {
        "category": "",
        "difficulty": "",
        "tag": "",
        "question": None,
        "rubric": None,
        "existing": False,
        "generate_attempts": 0,
        "rubric_attempts": 0,
        "generate_failures": [],
        "rubric_failures": [],
        "last_problems": [],
        "timings": [],
        "verdict": "",
        "discarded": "",
    }
    state.update(overrides)
    return state
