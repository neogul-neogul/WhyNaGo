"""4단계 — 승인된 문항을 data3.sql 형태의 INSERT로 뽑는다.

DB에 직접 넣지 않는다. 시드가 git에 남아 재현·리뷰 가능한 현재 구조를 유지한다.
"""

from __future__ import annotations

import json
from typing import Any

VARIABLE_PREFIX = "@p"
PROMPT_KEY = "promptVersion"


def render(questions: list[dict[str, Any]]) -> str:
    lines = [
        "-- 문항 생성 파이프라인 산출물. tools/question-pipeline 참고.",
        "-- data2.sql이 @e1~@e175를 쓰므로 세션 변수 접두사를 @p로 분리한다.",
        *_prompt_summary(questions),
        "",
    ]
    for index, question in enumerate(questions, start=1):
        variable = "%s%d" % (VARIABLE_PREFIX, index)
        lines += _prompt_comment(question)
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
        *_prompt_summary(questions),
        "",
    ]
    for question in questions:
        lines += _prompt_comment(question)
        lines.append(_update_rubric_by_title(question))
        lines.append("")
    return "\n".join(lines)


def _prompt_summary(questions: list[dict[str, Any]]) -> list[str]:
    """이 파일이 어느 프롬프트에서 나왔는지 첫머리에 밝힌다.

    리뷰 파일은 프롬프트를 고쳐도 남아 있어서, 한 파일에 두 버전이 섞일 수 있다.
    그래서 하나로 단정하지 않고 실제로 쓰인 것을 전부 적는다.
    """
    stamps = sorted({question[PROMPT_KEY] for question in questions if question.get(PROMPT_KEY)})
    return ["-- 프롬프트 %s" % ", ".join(stamps)] if stamps else []


def _prompt_comment(question: dict[str, Any]) -> list[str]:
    stamp = question.get(PROMPT_KEY)
    return ["-- promptVersion=%s" % stamp] if stamp else []


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
    """태그는 이름이 아니라 tag 테이블의 id 를 넣는다.

    question_tag 가 tag_id 를 참조하도록 정규화됐다(data3.sql 과 같은 형식).
    이름으로 넣던 형식(data2.sql)은 이제 부팅에서 죽는다.
    이 파일은 data-tag.sql 이 먼저 로드된 뒤에 실행돼야 한다.
    """
    rows = ",\n".join(
        "(%s, (SELECT id FROM tag WHERE name = %s))" % (variable, _quote(tag))
        for tag in question["tags"]
    )
    return "INSERT INTO question_tag (question_id, tag_id) VALUES\n%s;" % rows


def _quote(value: str) -> str:
    return "'%s'" % str(value).replace("\\", "\\\\").replace("'", "''")
