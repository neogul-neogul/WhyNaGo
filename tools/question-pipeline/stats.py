"""시드 실측치. 문서에 적힌 숫자가 맞는지 확인하는 용도다.

    python tools/question-pipeline/stats.py
"""

from __future__ import annotations

import statistics
from collections import Counter

from adapters import seed


def _lengths(questions: list[seed.Question]) -> str:
    characters = sorted(len(question.explanation) for question in questions)
    utf8_bytes = sorted(len(question.explanation.encode("utf-8")) for question in questions)
    return (
        f"{len(questions):>5}건   "
        f"자 {min(characters):>4}~{max(characters):<4} 평균 {round(statistics.mean(characters)):<4}   "
        f"바이트 {min(utf8_bytes):>4}~{max(utf8_bytes):<4} 평균 {round(statistics.mean(utf8_bytes))}"
    )


def main() -> None:
    questions = seed.load()
    essays = seed.essays(questions)
    choices = [question for question in questions if question.type != "ESSAY"]

    print(f"시드 문항 {len(questions)}건 (서술형 {len(essays)}, 객관식 {len(choices)})")
    print()
    print("explanation 길이")
    print(f"  전체   {_lengths(questions)}")
    print(f"  서술형 {_lengths(essays)}")
    print(f"  객관식 {_lengths(choices)}")
    print()

    tags = Counter(tag for question in questions for tag in question.tags)
    print(f"태그 {len(tags)}종 / 부착 {sum(tags.values())}건 / 문항당 평균 {sum(tags.values()) / len(questions):.1f}")
    print(f"  상위 5종 {tags.most_common(5)}")
    print(f"  1회만 쓰인 태그 {sum(1 for count in tags.values() if count == 1)}종")
    print()

    for label, key in (("카테고리", "category"), ("난이도", "difficulty")):
        counts = Counter(getattr(question, key) for question in essays)
        print(f"서술형 {label}  {dict(sorted(counts.items()))}")


if __name__ == "__main__":
    main()
