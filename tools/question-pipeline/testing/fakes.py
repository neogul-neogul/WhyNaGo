"""API 없이 그래프를 태우기 위한 가짜 LLM.

호출을 다 쓰고 나서 배선 버그가 터지는 걸 막는다.
기본 대본은 **모든 분기를 한 번씩 지나가게** 짜여 있다 — 중복 재생성, 목록 밖 태그 반려, 루브릭 반려, 승인.
"""

from __future__ import annotations

import json
from typing import Any

from adapters import seed
from similarity import duplicates
from core import state
from vocabulary import tags as tags_module

GENERATE_MARK = "서술형 문항 출제자"
RUBRIC_MARK = "채점 기준(루브릭)을 만드는 사람"


# prompts.yml 의 섹션 머리말과 같아야 한다. 바꾸면 대본이 태그를 못 읽어 dry-run 이 전부 반려된다.
TAG_MARK = "[주제 태그]"
DIFFICULTY_MARK = "난이도 기준"
BAD_TAG = "목록에 없는 태그"


class ScriptedClient:
    """첫 문항은 중복으로, 첫 루브릭은 배점 합 9로 내보내 재시도 경로를 태운다.

    bad_tag_turn 을 주면 그 차례에 목록 밖 태그를 내보내 폐쇄집합 게이트도 태운다.
    """

    def __init__(
        self,
        duplicate_of: seed.Question | None = None,
        bad_tag_turn: int | None = None,
    ) -> None:
        self.duplicate_of = duplicate_of
        self.bad_tag_turn = bad_tag_turn
        self.counts: dict[str, int] = {}
        self.calls: list[tuple[str, str]] = []
        self._explanation = ""
        self._heaviest_weight = 0

    def complete(self, system: str, user: str) -> str:
        self.calls.append((system, user))
        if GENERATE_MARK in system:
            return self._generate(system)
        if RUBRIC_MARK in system:
            return self._rubric(system)
        raise AssertionError("대본에 없는 호출이다 - %r" % system[:60])

    def _next(self, kind: str) -> int:
        self.counts[kind] = self.counts.get(kind, 0) + 1
        return self.counts[kind]

    def _generate(self, system: str) -> str:
        turn = self._next("generate")
        primary = _primary_tag_of(system)
        assigned = [primary] if primary else []
        if turn == self.bad_tag_turn:
            assigned = [primary, BAD_TAG] if primary else [BAD_TAG]
        if turn == 1 and self.duplicate_of is not None:
            self._explanation = self.duplicate_of.explanation
            return json.dumps(
                {
                    "title": self.duplicate_of.title,
                    "content": self.duplicate_of.content,
                    "explanation": self.duplicate_of.explanation,
                    "tags": assigned,
                },
                ensure_ascii=False,
            )
        self._explanation = (
            "커넥션 풀은 미리 만들어 둔 커넥션을 빌려 쓰고 돌려받는 구조라 매 요청마다 "
            "TCP 연결과 인증을 새로 맺는 비용을 없앱니다. 풀 크기를 늘리면 동시에 처리할 수 있는 "
            "요청이 늘지만, DB 서버가 감당할 수 있는 동시 실행 수를 넘어서면 컨텍스트 스위칭과 "
            "락 경합이 늘어 오히려 전체 처리량이 떨어집니다. 그래서 풀 크기는 애플리케이션이 아니라 "
            "DB의 코어 수와 디스크 처리 능력을 기준으로 정하는 것이 바람직합니다. 응답이 느려질 때 "
            "풀만 키우면 문제가 커지는 이유가 여기에 있습니다."
        )
        return json.dumps(
            {
                "title": "커넥션 풀 크기를 정하는 기준 %d" % turn,
                "content": "커넥션 풀이 성능에 도움이 되는 이유와, 풀 크기를 무작정 늘렸을 때 생기는 문제를 설명하시오.",
                "explanation": self._explanation,
                "tags": assigned,
            },
            ensure_ascii=False,
        )

    def _rubric(self, system: str) -> str:
        """채점 항목을 해설 문장에서 그대로 잘라 온다.

        하드코딩하면 기존 문항 백필을 태울 때 항목이 해설에 없어 전부 반려된다.
        해설에서 떼어 오면 어떤 문항이 들어와도 포함도 검사를 통과한다 — 프롬프트가
        실제 모델에 요구하는 것("해설의 표현을 그대로 가져다 쓴다")과도 같다.
        """
        turn = self._next("rubric")
        self._explanation = _explanation_of(system) or self._explanation
        points = _sentences(self._explanation)
        weights = _weights(len(points), total=9 if turn == 1 else 10)
        self._heaviest_weight = max(weights)
        return json.dumps(
            {
                "criteria": [
                    {"point": point, "weight": weight}
                    for point, weight in zip(points, weights)
                ],
                "followupScope": {
                    "allowed": ["개념 정의", "동작 순서", "장단점", "적용 상황"],
                    "forbidden": ["내부 구현 세부사항", "하위 컴포넌트 계층 구조"],
                },
            },
            ensure_ascii=False,
        )


MIN_CRITERIA = 3
MAX_CRITERIA = 4


def _explanation_of(system: str) -> str:
    start = system.find("정답 해설: ")
    end = system.find("\n태그:", start)
    if start < 0 or end < 0:
        return ""
    return system[start + len("정답 해설: ") : end].strip()


def _primary_tag_of(system: str) -> str:
    start = system.find(TAG_MARK)
    end = system.find(DIFFICULTY_MARK, start)
    if start < 0 or end < 0:
        return ""
    return system[start + len(TAG_MARK) : end].strip()




def _sentences(explanation: str) -> list[str]:
    """'~다.'로 끝나는 문장 단위. 모자라면 길이로 쪼갠다."""
    parts = [part.strip() for part in explanation.split("다. ") if part.strip()]
    parts = [part if part.endswith("다.") else part + "다." for part in parts][:MAX_CRITERIA]
    if len(parts) >= MIN_CRITERIA:
        return parts
    size = max(len(explanation) // MIN_CRITERIA, 1)
    return [explanation[index : index + size] for index in range(0, size * MIN_CRITERIA, size)]


def _weights(count: int, total: int) -> list[int]:
    """가장 무거운 항목이 하나로 정해지도록 앞에 몰아 준다."""
    weights = [1] * count
    weights[0] += total - count
    return weights


def semantic(query: str, candidates: list[tuple[str, str]]) -> tuple[str, float]:
    """중복 게이트용 의미 유사도 대역. 임베딩 없이 결정적으로 돈다.

    실제 판정은 bge-m3 코사인이지만 테스트에서 데몬을 띄울 수는 없다. 자카드로 대신하되,
    **판정 결과가 같아야 하는 구간에서만** 쓴다 — 거의 같은 문항이면 1.0에 가깝고
    무관하면 낮다. 컷 근처의 미세한 값을 시험하는 데는 쓰면 안 된다.
    """
    if not candidates:
        return ("", 0.0)
    grams = duplicates.ngrams(query)
    scored = [
        (duplicates.jaccard(grams, duplicates.ngrams(text)), title)
        for title, text in candidates
    ]
    score, title = max(scored)
    return (title, score)


def scripted_client(category: str = "DB") -> ScriptedClient:
    essays = [
        question for question in seed.essays(seed.load()) if question.category == category
    ]
    return ScriptedClient(duplicate_of=essays[0] if essays else None, bad_tag_turn=2)


def sample_state(tag: str | None = None) -> dict[str, Any]:
    picked = tags_module.find(tag) if tag else tags_module.plan(1, category="DB")[0]
    return state.initial(category=picked.category, difficulty="MEDIUM", tag=picked.name)


def existing_state(question: seed.Question) -> dict[str, Any]:
    return state.initial(
        category=question.category,
        difficulty=question.difficulty,
        existing=True,
        question={
            "title": question.title,
            "content": question.content,
            "explanation": question.explanation,
            "category": question.category,
            "difficulty": question.difficulty,
            "tags": list(question.tags),
        },
    )
