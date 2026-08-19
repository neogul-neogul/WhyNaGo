"""시드 SQL 파일에서 문항과 태그를 읽는다.

운영 DB를 건드리지 않고, 로컬 MySQL 컨테이너도 띄우지 않기 위해 시드 파일을 직접 읽는다.
시드가 곧 DB에 들어가는 내용이므로(spring.sql.init) 둘은 같은 데이터다.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, replace
from pathlib import Path

RESOURCES = Path(__file__).resolve().parents[3] / "src" / "main" / "resources"
SEED_GLOB = "data*.sql"

# 시드 파일마다 형식이 다르다. data.sql은 id 컬럼을 갖고 한 문장에 여러 행을 넣고,
# data2.sql은 id 없이 한 행씩 넣고 SET @eN = LAST_INSERT_ID() 로 참조한다.
# 컬럼 목록을 읽어 이름으로 매핑하고, 행은 따옴표·괄호 깊이를 보며 직접 쪼갠다.
_QUESTION = re.compile(
    r"INSERT\s+INTO\s+question\s*\((?P<columns>[^)]*)\)\s*VALUES\s*(?P<values>.*?);",
    re.DOTALL | re.IGNORECASE,
)
_VARIABLE = re.compile(r"SET\s+(?P<name>@\w+)\s*=\s*LAST_INSERT_ID\(\)\s*;", re.IGNORECASE)
_TAG_BLOCK = re.compile(
    r"INSERT\s+INTO\s+question_tag\s*\([^)]*\)\s*VALUES\s*(?P<rows>.*?);",
    re.DOTALL | re.IGNORECASE,
)
_TAG_ROW = re.compile(r"\(\s*(?P<key>@?\w+)\s*,\s*'(?P<name>(?:[^']|'')*)'\s*\)")

# 루브릭 UPDATE도 형식이 둘이다. 손으로 붙인 data3-rubric.sql은 title로 찾고,
# 파이프라인이 뽑는 data4-generated.sql은 같은 파일에서 잡은 세션 변수로 찾는다.
_RUBRIC_SET = r"UPDATE\s+question\s+SET\s+rubric\s*=\s*'(?:[^']|'')*'\s+WHERE\s+[^;]*?"
_RUBRIC_BY_TITLE = re.compile(_RUBRIC_SET + r"title\s*=\s*'(?P<title>(?:[^']|'')*)'", re.IGNORECASE)
_RUBRIC_BY_ID = re.compile(_RUBRIC_SET + r"id\s*=\s*(?P<id>@?\w+)", re.IGNORECASE)

REQUIRED_COLUMNS = ("title", "content", "type", "difficulty", "category")


@dataclass(frozen=True)
class Question:
    variable: str
    title: str
    content: str
    type: str
    difficulty: str
    category: str
    explanation: str
    tags: tuple[str, ...] = ()
    has_rubric: bool = False

    @property
    def text(self) -> str:
        return f"{self.title}\n{self.content}"


def seed_paths() -> list[Path]:
    """data*.sql 을 전부 읽는다.

    파일 하나만 읽으면 파이프라인이 직접 만든 문항(data4-generated.sql)이 다음 실행의
    중복 기준선에서 빠져 같은 주제를 또 만든다. 문항 INSERT가 없는 파일은 0건을 내므로
    글롭으로 잡아도 안전하고, 새 시드 파일이 생겨도 자동으로 포함된다.
    """
    return sorted(RESOURCES.glob(SEED_GLOB))


def load(paths: list[Path] | None = None) -> list[Question]:
    """루브릭 표시는 파일을 다 읽은 뒤에 붙인다.

    data3-rubric.sql은 UPDATE만 있고 문항은 다른 파일에 있어서, 파일 하나씩 판정하면
    어떤 문항에도 루브릭이 없다고 나온다.
    """
    seeds = [path.read_text(encoding="utf-8") for path in (seed_paths() if paths is None else paths)]
    questions = [question for seed in seeds for question in _load_one(seed)]
    titles, identifiers = _rubric_targets(seeds)
    return [
        replace(question, has_rubric=question.title in titles or question.variable in identifiers)
        for question in questions
    ]


def _load_one(seed: str) -> list[Question]:
    tags = _tags(seed)
    return [
        replace(question, tags=tuple(tags.get(question.variable, ())))
        for question in _questions(seed)
    ]


def _rubric_targets(seeds: list[str]) -> tuple[set[str], set[str]]:
    titles: set[str] = set()
    identifiers: set[str] = set()
    for seed in seeds:
        titles.update(_unquote(match.group("title")) for match in _RUBRIC_BY_TITLE.finditer(seed))
        identifiers.update(match.group("id") for match in _RUBRIC_BY_ID.finditer(seed))
    return titles, identifiers


def essays(questions: list[Question]) -> list[Question]:
    return [question for question in questions if question.type == "ESSAY"]


def needs_rubric(questions: list[Question]) -> list[Question]:
    """루브릭이 없는 서술형. 제목이 겹치는 문항은 뺀다.

    루브릭 UPDATE가 title로 문항을 찾으므로, 제목이 둘이면 엉뚱한 문항까지 같이 덮어쓴다.
    시드에 실제로 그런 쌍이 있다(정규화와 반정규화의 트레이드오프).
    """
    candidates = [question for question in essays(questions) if not question.has_rubric]
    counts: dict[str, int] = {}
    for question in essays(questions):
        counts[question.title] = counts.get(question.title, 0) + 1
    return [question for question in candidates if counts[question.title] == 1]


def ambiguous_titles(questions: list[Question]) -> list[str]:
    counts: dict[str, int] = {}
    for question in essays(questions):
        counts[question.title] = counts.get(question.title, 0) + 1
    return sorted(title for title, count in counts.items() if count > 1)


def _questions(seed: str) -> list[Question]:
    questions: list[Question] = []
    for match in _QUESTION.finditer(seed):
        columns = [column.strip().lower() for column in match.group("columns").split(",")]
        missing = [column for column in REQUIRED_COLUMNS if column not in columns]
        if missing:
            raise ValueError(f"문항 INSERT에 필요한 컬럼이 없다 - missing={missing}")

        for row in _rows(match.group("values")):
            fields = _split_values(row)
            if len(fields) != len(columns):
                raise ValueError(
                    f"컬럼 {len(columns)}개인데 값이 {len(fields)}개다 - head={fields[:1]}"
                )
            value = dict(zip(columns, fields))
            questions.append(
                Question(
                    variable=_identifier(seed, match.end(), value),
                    title=value["title"],
                    content=value["content"],
                    type=value["type"],
                    difficulty=value["difficulty"],
                    category=value["category"],
                    explanation=value.get("explanation", ""),
                )
            )
    return questions


def _identifier(seed: str, offset: int, value: dict[str, str]) -> str:
    """태그를 문항에 붙이기 위한 키.

    id 컬럼이 있으면 그 값이 키이고(data.sql), 없으면 INSERT 바로 뒤의
    SET @x = LAST_INSERT_ID() 가 키다(data2.sql).
    """
    if "id" in value:
        return value["id"]
    match = _VARIABLE.search(seed, offset)
    if match is None or _QUESTION.search(seed, offset, match.start()) is not None:
        return ""
    return match.group("name")


def _rows(values: str) -> list[str]:
    """VALUES 뒤의 (...), (...) 를 최상위 괄호 기준으로 쪼갠다."""
    rows: list[str] = []
    buffer: list[str] = []
    depth = 0
    quoted = False
    index = 0
    while index < len(values):
        character = values[index]
        if character == "'":
            if quoted and values[index + 1 : index + 2] == "'":
                buffer.append("''")
                index += 2
                continue
            quoted = not quoted
        elif not quoted and character == "(":
            depth += 1
            if depth == 1:
                buffer = []
                index += 1
                continue
        elif not quoted and character == ")":
            depth -= 1
            if depth == 0:
                rows.append("".join(buffer))
                index += 1
                continue
        if depth >= 1:
            buffer.append(character)
        index += 1
    return rows


def _tags(seed: str) -> dict[str, list[str]]:
    tags: dict[str, list[str]] = {}
    for block in _TAG_BLOCK.finditer(seed):
        for row in _TAG_ROW.finditer(block.group("rows")):
            tags.setdefault(row.group("key"), []).append(_unquote(row.group("name")))
    return tags


def _split_values(values: str) -> list[str]:
    """작은따옴표 문자열 안의 쉼표를 무시하고 VALUES 항목을 쪼갠다."""
    fields: list[str] = []
    buffer: list[str] = []
    quoted = False
    index = 0
    while index < len(values):
        character = values[index]
        if character == "'":
            if quoted and values[index + 1 : index + 2] == "'":
                buffer.append("''")
                index += 2
                continue
            quoted = not quoted
        elif character == "," and not quoted:
            fields.append(_unquote("".join(buffer)))
            buffer = []
            index += 1
            continue
        buffer.append(character)
        index += 1
    fields.append(_unquote("".join(buffer)))
    return fields


def _unquote(field: str) -> str:
    field = field.strip()
    if field.startswith("'") and field.endswith("'") and len(field) >= 2:
        field = field[1:-1]
    return field.replace("''", "'")
