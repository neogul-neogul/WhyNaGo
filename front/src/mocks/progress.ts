import type { ProgressMetric, WeeklyCard } from "@/types";
import { palette } from "@/lib/tokens";

// 진척도 지표 카드 — 더미
export const progressMetrics: ProgressMetric[] = [
  { label: "누적 학습일", value: "42", unit: "일", color: palette.ink },
  { label: "연속 학습일", value: "7", unit: "일", color: palette.streak },
  { label: "총 풀이 문제", value: "487", unit: "문제", color: palette.ink },
  { label: "총 정답", value: "342", unit: "개", color: palette.success },
  { label: "총 오답", value: "145", unit: "개", color: palette.danger },
  { label: "1일 1면접", value: "16", unit: "회", color: palette.ai },
];

// 주간 리포트 — 더미
export const weekRange = "2026.06.16 — 06.22";
export const weeklyDays = 6;
export const weeklyRate = 72;
export const weeklyCards: WeeklyCard[] = [
  { label: "총 풀이 문제", value: "58문제", delta: "+12", deltaColor: palette.success },
  { label: "연속 학습", value: "유지 중", delta: "7일", deltaColor: palette.streak },
  { label: "가장 많이 학습", value: "알고리즘", delta: "24문제", deltaColor: palette.soft },
  { label: "가장 많이 틀림", value: "운영체제", delta: "9오답", deltaColor: palette.danger },
  { label: "오답 복습 완료", value: "14개", delta: "+5", deltaColor: palette.success },
  { label: "1일 1면접", value: "4회", delta: "+1", deltaColor: palette.success },
];
