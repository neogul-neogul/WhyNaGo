import { apiFetch } from "@/lib/api";
import type { Position, UpdateProfileRequest, UserProfileResponse } from "@/types";

// 사용자 프로필 도메인 API

/** 직무 enum → 화면 라벨 */
export const POSITION_LABELS: Record<Position, string> = {
  BACKEND: "백엔드",
  FRONTEND: "프론트엔드",
  FULLSTACK: "풀스택",
};

function labelToEnum<T extends string>(labels: Record<T, string>, label: string): T | undefined {
  return (Object.keys(labels) as T[]).find((key) => labels[key] === label);
}

/** 화면 라벨 → 직무 enum */
export function positionFromLabel(label: string): Position | undefined {
  return labelToEnum(POSITION_LABELS, label);
}

/** 내 프로필 조회 */
export function fetchMyProfile(): Promise<UserProfileResponse> {
  return apiFetch<UserProfileResponse>("/api/users/me");
}

/** 프로필 수정 (부분 수정이 아니라 매 요청마다 전체 필드를 보낸다) */
export function updateMyProfile(request: UpdateProfileRequest): Promise<UserProfileResponse> {
  return apiFetch<UserProfileResponse>("/api/users/me", {
    method: "PATCH",
    body: request,
  });
}
