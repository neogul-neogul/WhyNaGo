import { apiFetch } from "@/lib/api";
import { CATEGORY_LABELS } from "@/lib/questions";
import { palette } from "@/lib/tokens";
import type {
  CategoryProgressResponse,
  ProgressMetric,
  ProgressResponse,
  ProgressSummaryResponse,
  ProgressTier,
  QuestionCategory,
} from "@/types";

// 진척도 도메인 API — 점수·티어 산정 규칙은 docs/DOMAIN.md 점수·티어 산정 정책 참고.

/** 티어 enum → 화면 라벨 */
export const TIER_LABELS: Record<ProgressTier, string> = {
  BRONZE: "브론즈",
  SILVER: "실버",
  GOLD: "골드",
  PLATINUM: "플래티넘",
  DIAMOND: "다이아",
};

/**
 * 티어 색. 메달 관례(브론즈 갈색·실버 회색…)를 유지하면서, 라벨·배지·툴팁 글씨로도 쓰이므로
 * 흰 배경 대비 4.5:1을 넘도록 어둡게 잡았다. 구간 라벨이 항상 함께 있어 색만으로 구분하지 않는다.
 */
export const TIER_COLORS: Record<ProgressTier, { line: string; bg: string; border: string }> = {
  BRONZE: { line: "#9C6A45", bg: "rgba(168,114,74,.10)", border: "rgba(168,114,74,.35)" },
  SILVER: { line: "#6C7681", bg: "rgba(124,135,148,.12)", border: "rgba(124,135,148,.38)" },
  GOLD: { line: "#926F17", bg: "rgba(188,143,30,.12)", border: "rgba(188,143,30,.38)" },
  PLATINUM: { line: "#2C817C", bg: "rgba(47,138,133,.12)", border: "rgba(47,138,133,.38)" },
  DIAMOND: { line: "#3E72C9", bg: "rgba(62,114,201,.12)", border: "rgba(62,114,201,.38)" },
};

/** 레이더는 "내 현재 점수" 한 계열이라 색도 하나만 쓴다 */
export const SERIES_LINE = "#2F6F8F";
export const SERIES_FILL = "rgba(47,111,143,.16)";

/** 카테고리 축 순서. 점수 순으로 정렬하면 도형이 매번 달라져 고정한다 */
const CATEGORY_ORDER: QuestionCategory[] = [
  "ALGORITHM",
  "DATA_STRUCTURE",
  "NETWORK",
  "OS",
  "DB",
  "LANGUAGE",
  "DESIGN_PATTERN",
];

/** 점수·티어·카테고리별 현황 조회 */
export function fetchProgress(): Promise<ProgressResponse> {
  return apiFetch<ProgressResponse>("/api/progress");
}

/** 진척도 상단 통계 조회 */
export function fetchProgressSummary(): Promise<ProgressSummaryResponse> {
  return apiFetch<ProgressSummaryResponse>("/api/progress/summary");
}

/** 상단 지표 카드 6개 */
export function toProgressMetrics(summary: ProgressSummaryResponse): ProgressMetric[] {
  return [
    { label: "누적 학습일", value: String(summary.cumulativeDays), unit: "일", color: palette.ink },
    { label: "연속 학습일", value: String(summary.streakDays), unit: "일", color: palette.streak },
    { label: "총 풀이 횟수", value: String(summary.totalQuestionCount), unit: "문제", color: palette.ink },
    { label: "총 정답", value: String(summary.totalCorrectCount), unit: "개", color: palette.success },
    { label: "총 오답", value: String(summary.totalWrongCount), unit: "개", color: palette.danger },
    { label: "1일 1면접", value: String(summary.completedInterviewCount), unit: "회", color: palette.ai },
  ];
}

/** 레이더 축 순서를 고정하기 위해 정해진 순서로 다시 정렬한다 */
export function orderedCategories(progress: ProgressResponse): CategoryProgressResponse[] {
  return CATEGORY_ORDER.map(
    (category) => progress.categories.find((c) => c.category === category),
  ).filter((category): category is CategoryProgressResponse => category !== undefined);
}

/** 카테고리 enum → 화면 라벨 */
export function categoryLabel(category: QuestionCategory): string {
  return CATEGORY_LABELS[category];
}

/**
 * 레이더 바깥 원이 나타낼 점수.
 * 카테고리별 만점은 문제은행 구성에 따라 달라 API로 알 수 없으므로 최고 점수를 10 단위로 올려 쓴다.
 */
export function radarMaxScore(categories: CategoryProgressResponse[]): number {
  const max = Math.max(...categories.map((c) => c.score), 0);
  return Math.max(10, Math.ceil(max / 10) * 10);
}
