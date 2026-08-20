"""프롬프트를 읽고 버전을 식별한다.

버전마다 파일을 따로 둔다(prompts-v1.yml, prompts-v2.yml). 한 파일을 덮어쓰면
"올리기 전에 커밋한다"는 사람 규율에 보존이 걸리는데, 실제로 그게 안 지켜져서
v1 을 잃을 뻔했다. 파일을 추가하는 방식은 규율 없이도 이전 버전이 남는다.
두 버전을 동시에 가질 수 있어야 A/B 도 한자리에서 된다.

**과거 버전 파일은 고치지 않는다.** 고칠 것이 있으면 새 버전을 만든다.

식별자는 둘이다. version 은 사람이 올리는 의미 단위이고, hash 는 그 파일이 실제로
어떤 텍스트인지를 가리는 정확한 값이다. 보존을 파일이 맡으므로 hash 는 이제
"얼어붙어야 할 파일이 정말 안 바뀌었나"를 검증한다. 둘을 합친 stamp 를 기록에 남긴다.
표기는 백엔드 로그(promptVersion=v4)에 맞춘다.
"""

from __future__ import annotations

import hashlib
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml

PROMPTS_DIR = Path(__file__).resolve().parent
NAME_FORMAT = "prompts-%s.yml"
CURRENT_VERSION = "v3"
VERSION_KEY = "version"
HASH_LENGTH = 8


@dataclass(frozen=True)
class Prompts:
    """섹션 조회는 dict 처럼 쓴다. version 은 섹션이 아니므로 text 에서 빠져 있다."""

    text: dict[str, str]
    version: str
    hash: str

    @property
    def stamp(self) -> str:
        return "%s+%s" % (self.version, self.hash)

    def __getitem__(self, section: str) -> str:
        return self.text[section]


def load(version: str | None = None, directory: Path = PROMPTS_DIR) -> Prompts:
    version = version or CURRENT_VERSION
    path = directory / (NAME_FORMAT % version)
    if not path.exists():
        raise FileNotFoundError("그런 프롬프트 버전이 없다 - %s" % path)

    raw = path.read_text(encoding="utf-8")
    document: dict[str, Any] = yaml.safe_load(raw)
    declared = document.pop(VERSION_KEY, None)
    # 버전이 파일명과 키 두 군데에 적힌다. 어긋나면 산출물의 stamp 가 거짓이 되므로 여기서 막는다.
    if declared != version:
        raise ValueError(
            "파일명과 %s 키가 다르다 - %s 안에 %r" % (VERSION_KEY, path.name, declared)
        )
    return Prompts(
        text=document,
        version=version,
        hash=hashlib.sha256(raw.encode("utf-8")).hexdigest()[:HASH_LENGTH],
    )


def versions(directory: Path = PROMPTS_DIR) -> list[str]:
    """있는 프롬프트 버전. --prompt 로 고를 수 있는 목록이다."""
    prefix, suffix = NAME_FORMAT.split("%s")
    return sorted(
        path.name[len(prefix) : -len(suffix)] for path in directory.glob(NAME_FORMAT % "*")
    )
