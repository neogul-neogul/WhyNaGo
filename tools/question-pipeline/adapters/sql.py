"""4단계 — 승인된 문항을 data3.sql 형태의 INSERT로 뽑는다.

DB에 직접 넣지 않는다. 시드가 git에 남아 재현·리뷰 가능한 현재 구조를 유지한다.
"""

from __future__ import annotations

import json
from typing import Any

VARIABLE_PREFIX = "@p"


def render(questions: list[dict[str, Any]]) -> str:
    lines = [
        "-- 문항 생성 파이프라인 산출물. tools/question-pipeline 참고.",
        "-- data2.sql이 @e1~@e175를 쓰므로 세션 변수 접두사를 @p로 분리한다.",
        "",
    ]
    for index, question in enumerate(questions, start=1):
        variable = "%s%d" % (VARIABLE_PREFIX, index)
        lines.append(_insert_question(question))
        lines.append("SET %s = LAST_INSERT_ID();" % variable)
        lines.append(_update_rubric(question, variable))
        if question.get("tags"):
            lines.append(_insert_tags(question, variable))
        lines.append("")
    return "\n".join(lines)


def render_rubrics(questions: list[dict[str, Any]]) -> str:
    """이미 있는 문항에 루브릭만 붙인다.

    data3-rubric.sql과 같은 형식으로 title에 매칭한다. 시드 문항은 id가 파일마다 다르게
    잡혀(숫자 리터럴 / 세션 변수) 안정적으로 참조할 수 있는 건 title뿐이다.
    제목이 겹치는 문항은 애초에 대상에서 빠진다(seed.needs_rubric).
    """
    lines = [
        "-- 기존 문항 루브릭 백필. tools/question-pipeline 참고.",
        "-- 문항을 새로 넣지 않는다. rubric 컬럼만 채운다.",
        "",
    ]
    for question in questions:
        lines.append(_update_rubric_by_title(question))
        lines.append("")
    return "\n".join(lines)


def _update_rubric_by_title(question: dict[str, Any]) -> str:
    body = json.dumps(question["rubric"], ensure_ascii=False, indent=2)
    return "UPDATE question SET rubric = %s\nWHERE type = 'ESSAY' AND title = %s;" % (
        _quote(body),
        _quote(question["title"]),
    )


def _insert_question(question: dict[str, Any]) -> str:
    return (
        "INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES\n"
        "(%s, %s, 'ESSAY', %s, %s,\n %s);"
        % (
            _quote(question["title"]),
            _quote(question["content"]),
            _quote(question["difficulty"]),
            _quote(question["category"]),
            _quote(question["explanation"]),
        )
    )


def _update_rubric(question: dict[str, Any], variable: str) -> str:
    """개행을 살려 pretty-print 한다. 한 줄로 밀면 diff를 못 읽는다."""
    body = json.dumps(question["rubric"], ensure_ascii=False, indent=2)
    return "UPDATE question SET rubric = %s\nWHERE id = %s;" % (_quote(body), variable)


def _insert_tags(question: dict[str, Any], variable: str) -> str:
    rows = ",\n".join("(%s, %s)" % (variable, _quote(tag)) for tag in question["tags"])
    return "INSERT INTO question_tag (question_id, name) VALUES\n%s;" % rows


def _quote(value: str) -> str:
    return "'%s'" % str(value).replace("\\", "\\\\").replace("'", "''")
