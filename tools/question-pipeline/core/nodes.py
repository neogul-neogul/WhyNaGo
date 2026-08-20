"""노드 함수. LangGraph를 import 하지 않는다.

그래야 노드를 단독으로 돌려볼 수 있고, LangGraph를 걷어내도 코드가 남는다.
`review`만 예외이며 graph.py에 있다.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from similarity import duplicates
from adapters import seed
from core import prompts as prompts_module
from vocabulary import tags as tags_module
from adapters.llm import LlmClient, complete_json
from core.state import GraphState

# 문항에 붙는 프롬프트 식별자의 키. 리뷰 파일과 시드 SQL 까지 이 이름 그대로 흘러간다.
PROMPT_KEY = "promptVersion"

# 네 번 정했다. 탐침 0.70 → 생성물 10문항 0.80 → 시드 전수 비교 0.90 → **재생 0.88.**
#
# 0.90은 시드 184문항 2,334쌍(같은 카테고리)을 전부 재서 정한 값이고, 그 범위에서는 지금도 옳다.
#
#   0.90 이상  2건  정규화와 반정규화 0.974 · TCP 흐름/혼잡 0.917   둘 다 진짜 중복
#   0.80~0.90  1건  인덱스 원리 x 커버링 인덱스 0.803             다른 문항인데 막힌다
#   0.75~0.80  4건  연결리스트 · 팩토리/커맨드 · 디스크 스케줄링 등     전부 다른 문항
#
# 0.88로 내린 근거는 **시드끼리가 아니라 생성물 대 기존 문항** 구간이다. 그때 "0.80~0.90이
# 비어 있다"고 본 것은 생성물 표본이 21건일 때였고, 31건으로 늘자 그 구간이 채워졌다.
# 기록 5개 31문항을 두 컷으로 재생하니 0.88에서 2건이 더 걸리고 둘 다 진짜 중복이다.
#
#   0.893  시스템 콜의 동작 방식 x 사용자 모드와 커널 모드의 역할   사람이 반려한 문항이다
#   0.886  쿠키와 세션의 차이와 저장 위치 x 쿠키와 세션의 개념과 차이점
#
# 0.86까지 내리면 사람이 승인한 쌍(멀티프로세스/IPC 0.870)이 막힌다. 거기가 경계다.
# 눈금 자료는 evidence/2026-08-20-cut-recalibration.md 에 있다.
SEMANTIC_CUT = 0.88
RETRIEVE_TOP = 5
REQUIRED_TOTAL_WEIGHT = 10
MIN_CRITERIA = 3
MAX_CRITERIA = 5
# 프롬프트는 250~430자를 요구한다. 여기는 그 지시를 못 지켰을 때의 하한선이라 여유를 둔다.
# 198자로 폐기된 적이 있다 — 2자 때문에 문항을 버리는 건 검사의 목적이 아니다.
MIN_EXPLANATION = 180
MAX_EXPLANATION = 500
GROUNDING_CUT = 0.20


@dataclass
class Deps:
    llm: LlmClient
    existing: list[seed.Question]
    prompts: prompts_module.Prompts | None = None
    # 승인 대기까지 간 것만이 아니라 **이번 실행에서 만든 문항 전부**다. 폐기된 것도 넣는다.
    # 폐기를 빼면 다음 문항이 그 존재를 모른 채 같은 주제를 또 만든다. 실제로 그렇게 물렸다 —
    # 컷을 넘은 두 쌍(0.918, 0.574)의 앞 문항이 모두 폐기된 것이었다.
    produced: list[dict[str, Any]] = field(default_factory=list)
    # (query, [(title, text)], top) -> [title]. None 이면 카테고리 전체를 그대로 보여준다.
    retriever: Any = None
    # (query, [(title, text)]) -> (title, score). None 이면 어휘 게이트만 돈다.
    semantic: Any = None
    # 중복 컷. 눈금을 다시 잴 때만 바꾼다(replay.py --cut).
    cut: float = SEMANTIC_CUT

    @classmethod
    def create(
        cls,
        llm: LlmClient,
        existing: list[seed.Question] | None = None,
        retriever: Any = None,
        semantic: Any = None,
        cut: float = SEMANTIC_CUT,
        prompt_version: str | None = None,
    ) -> "Deps":
        return cls(
            llm=llm,
            existing=seed.essays(seed.load()) if existing is None else existing,
            prompts=prompts_module.load(prompt_version),
            retriever=retriever,
            semantic=semantic,
            cut=cut,
        )


def generate(state: GraphState, deps: Deps) -> GraphState:
    """태그를 축으로 문항을 만든다.

    생성기에 기존 주제를 알려준다. 이게 없으면 같은 태그 안에서 가장 전형적인 발문을
    반복해 만든다. 중복 게이트는 사후 필터라 같은 주제를 계속 반려할 뿐이고,
    예방은 여기서만 된다.
    """
    primary = tags_module.find(state["tag"])
    system = deps.prompts["generate"] % (
        state["difficulty"],
        primary.name,
        primary.name,
        _selectable_tags(primary),
    )
    system += deps.prompts["existing"] % _existing_titles(primary, deps)
    failures = state.get("generate_failures") or []
    if failures:
        system += deps.prompts["generateRetry"] % _bullets(failures)
    if any(tags_module.OUT_OF_LIST in failure for failure in failures):
        system += deps.prompts["generateRetryTags"] % primary.name

    question = complete_json(deps.llm, system, "문항을 만들어라.")
    question["category"] = primary.category
    question["difficulty"] = state["difficulty"]
    question[PROMPT_KEY] = deps.prompts.stamp
    return {
        "question": question,
        "category": primary.category,
        "generate_attempts": state.get("generate_attempts", 0) + 1,
        "rubric_attempts": 0,
        "rubric_failures": [],
        "last_problems": [],
    }


def validate_question(state: GraphState, deps: Deps) -> GraphState:
    """문항 자체에 거는 검사. 실패하면 문항을 다시 만든다.

    태그 폐쇄집합은 프롬프트에 목록을 넣는 것만으로는 안 지켜진다. 목록의 '인덱스' 대신
    '인덱스 설계' 같은 변종이 나오는데, 그걸 통과시키면 어휘가 다시 늘어난다.

    해설 길이도 여기서 본다. `validate_rubric`에 두면 실패가 `rubric` 노드로 돌아가는데,
    루브릭을 다시 만들어도 해설은 그대로라 같은 길이로 세 번 떨어지고 폐기된다.
    실제로 그렇게 물렸다 — 194자로 세 번 실패한 기록이 evidence 에 있다.
    """
    question = state["question"] or {}
    primary = tags_module.find(state["tag"])
    problems = tags_module.violations(list(question.get("tags", [])), primary)

    length = len(question.get("explanation", ""))
    if not MIN_EXPLANATION <= length <= MAX_EXPLANATION:
        problems.append(
            "정답 해설이 %d자다. %d~%d자여야 한다." % (length, MIN_EXPLANATION, MAX_EXPLANATION)
        )

    title = (question.get("title") or "").strip()
    if title in tags_module.names():
        problems.append(
            "제목 '%s'가 태그 이름 그대로다. 무엇을 묻는지 드러나게 써라." % title
        )

    if not problems:
        return {"last_problems": []}
    return {
        "last_problems": problems,
        "generate_failures": state.get("generate_failures", []) + problems,
    }


def dedup(state: GraphState, deps: Deps) -> GraphState:
    """중복 검사. 의미 유사도 하나로 판정한다.

    어휘(3-gram 자카드) 단계가 앞에 있었으나 뺐다. 시드 16,836쌍을 전수 비교했더니
    **어휘가 잡고 임베딩이 놓치는 쌍이 0개**였다 — 어휘가 잡는 것은 임베딩이 전부 잡는다.
    반대로 임베딩만 잡은 쌍이 17개다. 판정에 기여하지 않는 단계였다.
    """
    question = state["question"]
    candidates = [
        (other.title, other.text)
        for other in deps.existing
        if other.category == question["category"]
    ] + [
        (other["title"], _text(other))
        for other in deps.produced
        if other["category"] == question["category"]
    ]

    match = _semantic_match(_text(question), candidates, deps)
    if match is None or match[1] < deps.cut:
        return {"last_problems": []}
    problem = "기존 문항 '%s'와 의미 유사도 %.3f로 중복이다. 다른 주제를 잡아라." % match
    return _duplicate(state, problem)


def _semantic_match(text: str, candidates, deps: Deps):
    """deps.semantic 이 없으면 검사를 끄겠다는 뜻이므로 통과시킨다.

    켜 뒀는데 죽는 것은 다르다. 유일한 게이트가 조용히 사라지면 그 배치 전체가
    무검증으로 나가므로 예외를 그대로 올린다. run.py 가 문항 단위로 '실패'로 기록한다.
    """
    if deps.semantic is None or not candidates:
        return None
    return deps.semantic(text, candidates)


def _duplicate(state: GraphState, problem: str) -> GraphState:
    return {
        "last_problems": [problem],
        "generate_failures": state.get("generate_failures", []) + [problem],
    }


def make_rubric(state: GraphState, deps: Deps) -> GraphState:
    question = state["question"]
    system = deps.prompts["rubric"] % (
        question["content"],
        question["explanation"],
        ", ".join(question.get("tags", [])),
    )
    if state.get("rubric_failures"):
        system += deps.prompts["rubricRetry"] % _bullets(state["rubric_failures"])

    rubric = complete_json(deps.llm, system, "채점 기준을 만들어라.")
    # backfill 은 문항을 생성하지 않고 여기부터 시작한다. generate 가 못 찍은 식별자를 여기서 찍는다.
    return {
        "rubric": rubric,
        "question": {**question, PROMPT_KEY: deps.prompts.stamp},
        "rubric_attempts": state.get("rubric_attempts", 0) + 1,
        "last_problems": [],
    }


def validate_rubric(state: GraphState, deps: Deps) -> GraphState:
    """결정적 검사. 셀프체크 앞에 두어 LLM 호출을 아낀다."""
    rubric = state["rubric"] or {}
    question = state["question"] or {}
    problems: list[str] = []

    criteria = rubric.get("criteria") or []
    if not MIN_CRITERIA <= len(criteria) <= MAX_CRITERIA:
        problems.append("채점 항목이 %d개다. %d~%d개여야 한다." % (len(criteria), MIN_CRITERIA, MAX_CRITERIA))

    total = sum(int(item.get("weight", 0)) for item in criteria)
    if total != REQUIRED_TOTAL_WEIGHT:
        problems.append("배점 합이 %d이다. 정확히 %d이어야 한다." % (total, REQUIRED_TOTAL_WEIGHT))

    explanation_grams = duplicates.ngrams(question.get("explanation", ""))
    for item in criteria:
        point = item.get("point", "")
        grounding = duplicates.containment(duplicates.ngrams(point), explanation_grams)
        if grounding < GROUNDING_CUT:
            problems.append(
                "채점 항목 '%s'가 정답 해설에 없는 내용이다(포함도 %.2f)." % (point, grounding)
            )

    scope = rubric.get("followupScope") or {}
    if not scope.get("allowed"):
        problems.append("followupScope.allowed가 비어 있다.")
    if not scope.get("forbidden"):
        problems.append("followupScope.forbidden이 비어 있다.")

    return _problems(state, problems)


def _problems(state: GraphState, problems: list[str]) -> GraphState:
    if not problems:
        return {"last_problems": []}
    return {
        "last_problems": problems,
        "rubric_failures": state.get("rubric_failures", []) + problems,
    }



def _selectable_tags(primary: tags_module.Tag) -> str:
    siblings = sorted(tags_module.names(primary.category) - {primary.name})
    return "\n".join("  - %s" % name for name in siblings)


def _existing_titles(primary: tags_module.Tag, deps: Deps) -> str:
    """생성기에 보여줄 '이미 있는 주제' 목록.

    카테고리 전체를 넣으면 DB만 28개, NETWORK는 40개가 넘는다. 목록이 길수록 초점이
    흐려지고, 정작 겹칠 문항이 그 안에 묻힌다. 임베딩이 있으면 **의미적으로 가까운 것만**
    골라 넣는다. 없으면 카테고리 전체로 물러선다.
    """
    candidates = [
        (question.title, question.text)
        for question in deps.existing
        if question.category == primary.category
    ] + [
        (question["title"], _text(question))
        for question in deps.produced
        if question["category"] == primary.category
    ]
    if not candidates:
        return ""

    query = primary.name
    nearest = _nearest_titles(query, candidates, deps)
    return "\n".join("  - %s" % title for title in nearest)


def _nearest_titles(query: str, candidates: list[tuple[str, str]], deps: Deps) -> list[str]:
    if deps.retriever is None:
        return [title for title, _ in candidates]
    try:
        ranked = deps.retriever(query, candidates, RETRIEVE_TOP)
    except Exception:
        # 검색이 죽어도 생성은 돌아야 한다. 예방이 약해질 뿐 게이트는 그대로다.
        return [title for title, _ in candidates]
    return ranked


def _text(question: dict[str, Any]) -> str:
    return "%s\n%s" % (question.get("title", ""), question.get("content", ""))


def _bullets(failures: list[str]) -> str:
    return "\n".join("- %s" % failure for failure in failures)
