import type { AdminSolveResult, ProgressTier, QuestionCategory, QuestionDifficulty } from "@/types";
import Badge from "@/components/ui/Badge";

// 관리자 화면은 사용자 화면과 달리 enum 코드를 그대로 노출한다 (운영자가 DB 값과 대조해야 하므로).

const DIFFICULTY_TEXT: Record<QuestionDifficulty, string> = {
  HIGH: "text-danger",
  MEDIUM: "text-warning",
  LOW: "text-success",
};

export function CategoryBadge({ category }: { category: QuestionCategory }) {
  return <Badge tone="accent">{category}</Badge>;
}

export function TypeBadge({ type }: { type: string }) {
  return <Badge tone="ai">{type}</Badge>;
}

/** 테이블 셀용 난이도 — 배경 없이 색만 입힌다 */
export function DifficultyText({ difficulty }: { difficulty: QuestionDifficulty }) {
  return (
    <span className={`text-[12.5px] font-bold ${DIFFICULTY_TEXT[difficulty]}`}>{difficulty}</span>
  );
}

/** 상세 화면 헤더용 난이도 — 배경까지 있는 배지 */
export function DifficultyBadge({ difficulty }: { difficulty: QuestionDifficulty }) {
  const tone = difficulty === "HIGH" ? "danger" : difficulty === "MEDIUM" ? "warning" : "success";
  return <Badge tone={tone}>{difficulty}</Badge>;
}

export function TierBadge({ tier }: { tier: ProgressTier }) {
  return (
    <Badge tone={tier === "DIAMOND" ? "ink" : "neutral"} className="tracking-[0.3px]">
      {tier}
    </Badge>
  );
}

/** 풀이 결과(완료 · 오답 · 복습) / 정오답 표시 */
export function ResultBadge({ result }: { result: AdminSolveResult | "정답" }) {
  const tone = result === "오답" ? "danger" : result === "복습" ? "warning" : "success";
  return <Badge tone={tone}>{result}</Badge>;
}
