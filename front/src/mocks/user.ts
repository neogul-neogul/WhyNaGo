import type { CurrentUser, LearningStats } from "@/types";

// 로그인한 사용자 (더미)
export const currentUser: CurrentUser = {
  name: "지민",
  role: "백엔드 취준생",
  email: "jimin.dev@gmail.com",
  initial: "지",
};

// 학습 통계 (더미)
export const learningStats: LearningStats = {
  streakDays: 7,
  cumulativeDays: 42,
};
