import { apiFetch } from "@/lib/api";
import type {
  CreateProblemSetRequest,
  CreateProblemSetResponse,
  ProblemSetDetailResponse,
  ProblemSetMembershipResponse,
  ProblemSetSummaryResponse,
} from "@/types";

// 문제집 도메인 API — 유튜브 재생목록과 같은 개념. 항상 본인만 볼 수 있다.

/** 내 문제집 목록 조회 (최신순) */
export function fetchProblemSets(): Promise<ProblemSetSummaryResponse[]> {
  return apiFetch<ProblemSetSummaryResponse[]>("/api/problem-sets");
}

/** 문제집 상세 조회 */
export function fetchProblemSetDetail(problemSetId: number): Promise<ProblemSetDetailResponse> {
  return apiFetch<ProblemSetDetailResponse>(`/api/problem-sets/${problemSetId}`);
}

/** 특정 문제 기준으로 내 문제집의 저장 여부를 함께 조회 (저장 모달용) */
export function fetchProblemSetMembership(questionId: number): Promise<ProblemSetMembershipResponse[]> {
  return apiFetch<ProblemSetMembershipResponse[]>(`/api/problem-sets/membership?questionId=${questionId}`);
}

/** 문제집 생성. questionId를 함께 보내면 생성과 동시에 그 문제가 담긴다 */
export function createProblemSet(request: CreateProblemSetRequest): Promise<CreateProblemSetResponse> {
  return apiFetch<CreateProblemSetResponse>("/api/problem-sets", {
    method: "POST",
    body: request,
  });
}

/** 문제집에 문제를 담는다 (멱등 — 이미 담겨 있으면 아무 일도 하지 않는다) */
export function addProblemSetItem(problemSetId: number, questionId: number): Promise<void> {
  return apiFetch<void>(`/api/problem-sets/${problemSetId}/items/${questionId}`, { method: "PUT" });
}

/** 문제집에서 문제를 뺀다 (멱등 — 없어도 아무 일도 하지 않는다) */
export function removeProblemSetItem(problemSetId: number, questionId: number): Promise<void> {
  return apiFetch<void>(`/api/problem-sets/${problemSetId}/items/${questionId}`, { method: "DELETE" });
}

/** 문제집을 삭제한다 (담긴 문제도 함께 삭제되며, 문제 자체는 영향 없음) */
export function deleteProblemSet(problemSetId: number): Promise<void> {
  return apiFetch<void>(`/api/problem-sets/${problemSetId}`, { method: "DELETE" });
}

/** ISO LocalDateTime("2026-06-25T10:00:00") → "2026.06.25" */
export function formatProblemSetDate(updatedAt: string): string {
  return updatedAt.slice(0, 10).replaceAll("-", ".");
}
