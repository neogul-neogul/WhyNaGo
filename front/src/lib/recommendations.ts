import { apiFetch } from "@/lib/api";
import type { RecommendationResponse, WeakTagsResponse } from "@/types";

/**
 * 현재 사용자의 학습 이력에 맞춘 문항을 가져온다.
 * 서버는 생성이 어려운 경우 기존 문제로 대체하므로, 화면은 같은 흐름으로 계속 진행한다.
 */
export function fetchRecommendations(): Promise<RecommendationResponse> {
  return apiFetch<RecommendationResponse>("/api/recommendations/questions");
}

/** 현재 사용자의 전체 풀이 이력 기반 취약 태그를 조회한다. */
export function fetchWeakTags(): Promise<WeakTagsResponse> {
  return apiFetch<WeakTagsResponse>("/api/recommendations/weak-tags");
}
