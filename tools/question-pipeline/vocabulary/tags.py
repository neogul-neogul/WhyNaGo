"""태그 어휘. tags.yml 을 읽어 폐쇄집합으로 다룬다."""

from __future__ import annotations

from dataclasses import dataclass
from functools import lru_cache
from pathlib import Path

import yaml

TAGS_PATH = Path(__file__).resolve().parent / "tags.yml"

MIN_TAGS = 1
MAX_TAGS = 3
OUT_OF_LIST = "목록에 없는 태그다"
DIFFICULTIES = ("LOW", "MEDIUM", "HIGH")


@dataclass(frozen=True)
class Tag:
    """태그 하나. **상한도 비중도 없다.**

    사전(docs/TAG.md)이 태그별 비중을 정하지 않는다. 같은 태그·같은 난이도로 여러 문항을
    허용하고, 중복은 RAG 주입과 유사도 게이트가 막는다.
    """

    name: str
    category: str


class UnknownTag(Exception):
    pass


@lru_cache(maxsize=1)
def load(path: Path | None = None) -> tuple[Tag, ...]:
    document = yaml.safe_load((path or TAGS_PATH).read_text(encoding="utf-8"))
    catalog = [
        Tag(name=name, category=category)
        for category, names_ in document["categories"].items()
        for name in names_
    ]
    _reject_duplicates(catalog)
    return tuple(catalog)


def categories() -> list[str]:
    return sorted({tag.category for tag in load()})


def by_category(category: str) -> list[Tag]:
    found = [tag for tag in load() if tag.category == category]
    if not found:
        raise UnknownTag("목록에 없는 카테고리다 - %s (있는 것: %s)" % (category, ", ".join(categories())))
    return found


def find(name: str) -> Tag:
    for tag in load():
        if tag.name == name:
            return tag
    raise UnknownTag("목록에 없는 태그다 - %s" % name)


def names(category: str | None = None) -> set[str]:
    pool = by_category(category) if category else load()
    return {tag.name for tag in pool}


def cluster_plan(count: int, category: str | None = None, per_tag: int = 3) -> list[Tag]:
    """태그를 뭉쳐서 뽑는다. 한 태그에 per_tag 개씩 몰아 준다.

    `plan` 이 넓게 훑는다면 이쪽은 같은 태그를 여러 번 준다. 게이트가 같은 태그 안의
    중복을 잡는지 보려면 이 배분이 필요하다.
    """
    pool = by_category(category) if category else list(load())
    picked: list[Tag] = []
    for tag in pool:
        if len(picked) >= count:
            break
        picked += [tag] * min(per_tag, count - len(picked))
    return picked


def plan(count: int, category: str | None = None) -> list[Tag]:
    """사전 순서대로 태그를 count개 고른다.

    한 바퀴를 다 돌기 전에는 같은 태그를 두 번 주지 않는다 — TAG.md 3-6(회차 내 주 태그
    중복 금지)과 3-7(모든 태그가 최소 한 번은 주 태그)이 요구하는 배분이다.
    난수를 쓰지 않으므로 같은 인자면 같은 결과가 나온다.
    """
    pool = by_category(category) if category else list(load())
    return [pool[index % len(pool)] for index in range(count)]




def violations(assigned: list[str], primary: Tag) -> list[str]:
    """붙은 태그가 폐쇄집합 규칙을 지키는지 본다."""
    problems: list[str] = []
    if not MIN_TAGS <= len(assigned) <= MAX_TAGS:
        problems.append("태그가 %d개다. %d~%d개여야 한다." % (len(assigned), MIN_TAGS, MAX_TAGS))
    if primary.name not in assigned:
        problems.append("주제 태그 '%s'가 빠졌다. 반드시 포함해야 한다." % primary.name)

    allowed = names(primary.category)
    unknown = [tag for tag in assigned if tag not in allowed]
    if unknown:
        problems.append(
            "%s - %s. %s 에서만 골라라 - %s"
            % (OUT_OF_LIST, ", ".join(unknown), primary.category, ", ".join(sorted(allowed)))
        )

    duplicated = sorted({tag for tag in assigned if assigned.count(tag) > 1})
    if duplicated:
        problems.append("같은 태그를 여러 번 붙였다 - %s" % ", ".join(duplicated))
    return problems


def _reject_duplicates(catalog: list[Tag]) -> None:
    seen = [tag.name for tag in catalog]
    duplicated = sorted({name for name in seen if seen.count(name) > 1})
    if duplicated:
        raise ValueError("tags.yml 에 같은 태그가 두 번 있다 - %s" % ", ".join(duplicated))
