import type {
  AdminMemberAnomaly,
  AdminMemberStatus,
  ProgressTier,
  QuestionCategory,
  QuestionDifficulty,
  QuestionReviewStatus,
} from "@/types";
import { diffTone } from "@/lib/badges";
import { DIFFICULTY_LABELS } from "@/lib/questions";
import Badge from "@/components/ui/Badge";

// 검수 대기는 "할 일"이라 주의를 끌어야 하고, 거절은 되돌릴 수 없는 판정이라 위험색을 쓴다.
const REVIEW_STATUS_TONE: Record<QuestionReviewStatus, "success" | "warning" | "danger"> = {
  APPROVED: "success",
  PENDING: "warning",
  REJECTED: "danger",
};

const MEMBER_STATUS_TONE: Record<AdminMemberStatus, "success" | "danger" | "neutral"> = {
  활성: "success",
  정지: "danger",
  탈퇴: "neutral",
};

// 관리자 화면은 사용자 화면과 달리 enum 코드를 그대로 노출한다 (운영자가 DB 값과 대조해야 하므로).
// 단 난이도는 예외로, 사용자 화면과 같은 상/중/하 라벨을 쓴다.

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
    <span className={`text-[12.5px] font-bold ${DIFFICULTY_TEXT[difficulty]}`}>
      {DIFFICULTY_LABELS[difficulty]}
    </span>
  );
}

/** 상세 화면 헤더용 난이도 — 배경까지 있는 배지 */
export function DifficultyBadge({ difficulty }: { difficulty: QuestionDifficulty }) {
  const label = DIFFICULTY_LABELS[difficulty];
  return <Badge tone={diffTone(label)}>{label}</Badge>;
}

/** 문항 검수 상태 — 관리자 목록에만 나온다 (공개 목록은 APPROVED만 보이므로 배지가 필요 없다) */
export function ReviewStatusBadge({ status }: { status: QuestionReviewStatus }) {
  return <Badge tone={REVIEW_STATUS_TONE[status]}>{status}</Badge>;
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
