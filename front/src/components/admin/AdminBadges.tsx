import type {
  AdminMemberAnomaly,
  AdminMemberStatus,
  AdminQuestionStatus,
  ProgressTier,
  QuestionCategory,
  QuestionDifficulty,
} from "@/types";
import Badge from "@/components/ui/Badge";

const STATUS_TONE: Record<AdminQuestionStatus, "success" | "warning" | "neutral"> = {
  PUBLISHED: "success",
  DRAFT: "warning",
  ARCHIVED: "neutral",
};

const MEMBER_STATUS_TONE: Record<AdminMemberStatus, "success" | "danger" | "neutral"> = {
  활성: "success",
  정지: "danger",
  탈퇴: "neutral",
};

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

export function StatusBadge({ status }: { status: AdminQuestionStatus }) {
  return <Badge tone={STATUS_TONE[status]}>{status}</Badge>;
}

export function MemberStatusBadge({ status }: { status: AdminMemberStatus }) {
  return <Badge tone={MEMBER_STATUS_TONE[status]}>{status}</Badge>;
}

export function AnomalyBadge({ anomaly }: { anomaly?: AdminMemberAnomaly }) {
  if (!anomaly) return <span className="text-icon">-</span>;
  return <Badge tone={anomaly.tone}>{anomaly.label}</Badge>;
}

export function TierBadge({ tier }: { tier: ProgressTier }) {
  return (
    <Badge tone={tier === "DIAMOND" ? "ink" : "neutral"} className="tracking-[0.3px]">
      {tier}
    </Badge>
  );
}
