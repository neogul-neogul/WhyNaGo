"""evidence 에 남은 문항을 **지금의 중복 게이트**에 다시 태운다.

게이트를 고칠 때마다 "그때 놓친 것을 이제 잡는가"와 "정상 문항을 막지는 않는가"를
같은 자료로 다시 잴 수 있어야 한다. 그 자료가 evidence 의 실제 실행 기록이다.

    uv run replay.py                     # 모든 기록
    uv run replay.py --file 2026-08-18-cookie-same-tag.json
    uv run replay.py --no-semantic       # 어휘 게이트만 (대조군)
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any

from core import nodes
from similarity import embeddings
from testing import fakes

EVIDENCE = Path(__file__).resolve().parent / "evidence"

# 파일별 카테고리. 기록에는 카테고리가 문항마다 적혀 있지 않다.
CATEGORIES = {
    "2026-08-17-network-low.json": "NETWORK",
    "2026-08-18-db-tag-baseline.json": "DB",
    "2026-08-18-cookie-same-tag.json": "NETWORK",
    "2026-08-18-cookie-after-fixes.json": "NETWORK",
}


def _questions(document: dict[str, Any]) -> list[dict[str, Any]]:
    """기록 형식이 두 가지다 — questions 직접, 또는 run1/run2 아래."""
    if "questions" in document:
        return document["questions"]
    collected: list[dict[str, Any]] = []
    for value in document.values():
        if isinstance(value, dict) and "questions" in value:
            collected += value["questions"]
    return collected


def replay(path: Path, semantic: Any) -> dict[str, Any]:
    document = json.loads(path.read_text(encoding="utf-8"))
    category = CATEGORIES.get(path.name, "DB")
    produced: list[dict[str, Any]] = []
    rows: list[dict[str, Any]] = []

    for item in _questions(document):
        question = {
            "title": item["title"],
            "content": item["content"],
            "category": category,
        }
        deps = nodes.Deps.create(fakes.ScriptedClient(), semantic=semantic)
        # 그때 승인된 문항이 나중에 시드에 들어간 경우가 있다. 그대로 두면 자기 자신과
        # 유사도 1.000 으로 걸려 재생이 무의미해진다.
        deps.existing = [
            other for other in deps.existing if other.title != question["title"]
        ]
        deps.produced = list(produced)
        problems = nodes.dedup({"question": question}, deps)["last_problems"]
        rows.append(
            {
                "title": item["title"],
                "당시": item.get("status", ""),
                "지금": "차단" if problems else "통과",
                "사유": problems[0] if problems else "",
            }
        )
        produced.append(question)

    return {"파일": path.name, "카테고리": category, "결과": rows}


def render(report: dict[str, Any]) -> str:
    lines = ["", "=" * 78, "%s  (%s)" % (report["파일"], report["카테고리"]), "=" * 78]
    blocked = 0
    for row in report["결과"]:
        lines.append("  %-4s %-46.46s %s" % (row["지금"], row["title"], row["당시"]))
        if row["사유"]:
            blocked += 1
            lines.append("       %s" % row["사유"][:92])
    lines.append("  → %d개 중 %d개 차단" % (len(report["결과"]), blocked))
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="기록된 문항을 현재 게이트에 다시 태운다")
    parser.add_argument("--file", help="evidence 안의 파일명. 없으면 전부")
    parser.add_argument("--no-semantic", action="store_true", help="어휘 게이트만 쓴다")
    parser.add_argument("--json", action="store_true", help="JSON 으로 출력한다")
    arguments = parser.parse_args()

    semantic = None if arguments.no_semantic else embeddings.nearest
    names = [arguments.file] if arguments.file else sorted(CATEGORIES)
    reports = [replay(EVIDENCE / name, semantic) for name in names]

    if arguments.json:
        print(json.dumps(reports, ensure_ascii=False, indent=2))
        return
    for report in reports:
        print(render(report))


if __name__ == "__main__":
    main()
