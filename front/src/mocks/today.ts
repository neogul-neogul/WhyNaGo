import type { LearningMenuItem, TodayGoal, TodayMetric } from "@/types";

// 오늘의 학습 목표 진행 상황 (더미)
export const todayGoal: TodayGoal = {
  target: 10,
  current: 12,
  completed: true,
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
    accentBg: "#EEF0FF",
    accentFg: "#4F46E5",
  },
  {
    key: "wrong",
    title: "오답노트 복습",
    description: "미복습 3문제 대기 중",
    href: "/wrong",
    icon: "wrong",
    accentBg: "#FEF2E8",
    accentFg: "#C2410C",
  },
  {
    key: "interview",
    title: "1일 1면접",
    description: "오늘 면접 아직 진행 전",
    href: "/interview",
    icon: "interview",
    accentBg: "#F0EDFF",
    accentFg: "#6D28D9",
    badge: "AI",
  },
  {
    key: "mock",
    title: "모의 진단",
    description: "실력 등급 다시 확인하기",
    href: "/mock",
    icon: "mock",
    accentBg: "#E8F5EE",
    accentFg: "#16A34A",
  },
];
