import type { LearningMenuItem, LearningStats, TodayGoal, TodayMetric } from "@/types";
import { palette } from "@/lib/tokens";

// 오늘의 학습 목표 진행 상황 (더미)
export const todayGoal: TodayGoal = {
  target: 10,
  current: 12,
  completed: true,
};

// 게스트 홈 배너 프리뷰용 더미 (실제 값 아님, 예시 화면 안내용)
export const guestPreviewGoal: TodayGoal = {
  target: 10,
  current: 6,
  completed: false,
};

export const guestPreviewStats: LearningStats = {
  streakDays: 7,
  cumulativeDays: 23,
};

// 오늘 지표 카드 (더미)
export const todayMetrics: TodayMetric[] = [
  { key: "solved", label: "오늘 푼 문제", value: "12", unit: "문제", tone: "default" },
  { key: "interview", label: "오늘 면접", value: "미완료", note: "· 1일 1면접", tone: "warning" },
  { key: "review", label: "오늘 오답 복습", value: "완료", note: "· 4문제", tone: "success" },
];

// 오늘 완료 가능한 학습 메뉴 (더미)
export const learningMenu: LearningMenuItem[] = [
  {
    key: "solve",
    title: "문제 풀이",
    description: "객관식 · 서술형 · 카테고리 · 난이도별",
    href: "/solve",
    icon: "solve",
    accentBg: palette.accentBg,
    accentFg: palette.accent,
  },
  {
    key: "collections",
    title: "문제집",
    description: "저장한 문제 다시 풀어보기",
    href: "/collections",
    icon: "collections",
    accentBg: palette.successPale,
    accentFg: palette.success,
  },
  {
    key: "wrong",
    title: "오답노트 복습",
    description: "미복습 3문제 대기 중",
    href: "/wrong",
    icon: "wrong",
    accentBg: palette.alertTint,
    accentFg: palette.alert,
  },
  {
    key: "interview",
    title: "1일 1면접",
    description: "오늘 면접 아직 진행 전",
    href: "/interview",
    icon: "interview",
    accentBg: palette.aiBg,
    accentFg: palette.ai,
    badge: "AI",
  },
];
