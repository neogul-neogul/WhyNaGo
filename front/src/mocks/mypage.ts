import type { Profile } from "@/types";

// 마이페이지 프로필 초기값 — 더미
export const defaultProfile: Profile = {
  nickname: "지민",
  email: "jimin.dev@gmail.com",
  job: "백엔드 개발 준비생",
  goal: "10",
  bio: "CS 기초를 탄탄히 다지는 중입니다.",
};

// 마이페이지 요약 통계 — 더미
export const mypageStats = [
  { label: "푼 문제", value: "128", unit: "개" },
  { label: "연속 학습", value: "12", unit: "일" },
  { label: "오답 복습률", value: "86", unit: "%" },
  { label: "평균 등급", value: "B+", unit: "" },
];
