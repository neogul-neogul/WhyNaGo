import { apiFetch } from "@/lib/api";
import type { UserProfileResponse } from "@/types";

// 사용자 프로필 도메인 API

/** 내 프로필(최소 학습 목표 등) 조회 */
export function fetchMyProfile(): Promise<UserProfileResponse> {
  return apiFetch<UserProfileResponse>("/api/users/me");
}

/** 최소 학습 목표 수정 */
export function updateDailyGoal(dailyGoal: number): Promise<UserProfileResponse> {
  return apiFetch<UserProfileResponse>("/api/users/me", {
    method: "PATCH",
    body: { dailyGoal },
  });
}