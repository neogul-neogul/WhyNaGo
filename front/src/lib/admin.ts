import { apiFetch } from "@/lib/api";
import { DIFFICULTY_LABELS, type QuestionFilters, type QuestionPaging } from "@/lib/questions";
import type {
  AdminQuestionDetailResponse,
  AdminQuestionResponse,
  MultipleChoiceStatisticsResponse,
  PageResponse,
  QuestionCategory,
  QuestionDifficulty,
  QuestionTypeCode,
} from "@/types";

// 관리자 도메인 API + 표시 형식 변환
// 관리자 화면은 사용자 화면과 달리 enum 코드를 그대로 노출하므로 라벨 매핑을 두지 않는다.

/** 필터 드롭다운에 노출하는 카테고리 (화면 표시 순서) */
export const ADMIN_CATEGORIES: QuestionCategory[] = [
  "NETWORK",
  "DB",
  "OS",
  "ALGORITHM",
  "DATA_STRUCTURE",
  "LANGUAGE",
  "DESIGN_PATTERN",
];

export const ADMIN_DIFFICULTIES: QuestionDifficulty[] = ["HIGH", "MEDIUM", "LOW"];

/** 난이도 필터·폼 드롭다운에 노출하는 라벨 (상/중/하) — 값은 enum으로 되돌려 서버에 보낸다 */
export const ADMIN_DIFFICULTY_LABELS: string[] = ADMIN_DIFFICULTIES.map(
  (difficulty) => DIFFICULTY_LABELS[difficulty],
);

export const ADMIN_QUESTION_TYPES: QuestionTypeCode[] = ["MULTIPLE_CHOICE", "ESSAY"];

/** 관리자 문제 목록 한 페이지의 문항 수 */
export const ADMIN_QUESTION_PAGE_SIZE = 8;

/** 관리자 문제 목록 조회 — 문제별 풀이수·정답률을 함께 받는다 */
export function fetchAdminQuestions(
  filters: QuestionFilters = {},
  paging: QuestionPaging = {},
): Promise<PageResponse<AdminQuestionResponse>> {
  const params = new URLSearchParams();
  if (filters.type) params.set("type", filters.type);
  if (filters.difficulty) params.set("difficulty", filters.difficulty);
  if (filters.category) params.set("category", filters.category);
  if (filters.keyword) params.set("q", filters.keyword);
  params.set("page", String(paging.page ?? 0));
  params.set("size", String(paging.size ?? ADMIN_QUESTION_PAGE_SIZE));
  return apiFetch<PageResponse<AdminQuestionResponse>>(`/api/admin/questions?${params.toString()}`);
}

/** 관리자 문제 상세 조회 — 선택지의 정답 여부까지 내려온다 */
export function fetchAdminQuestionDetail(questionId: number): Promise<AdminQuestionDetailResponse> {
  return apiFetch<AdminQuestionDetailResponse>(`/api/admin/questions/${questionId}`);
}

/** 객관식 문제 통계 조회 — 서술형에 호출하면 400이므로 유형을 확인하고 호출한다 */
export function fetchAdminQuestionStatistics(
  questionId: number,
): Promise<MultipleChoiceStatisticsResponse> {
  return apiFetch<MultipleChoiceStatisticsResponse>(
    `/api/admin/questions/${questionId}/statistics`,
  );
}

/** 백엔드는 비율을 숫자로 내려주므로 % 표기는 화면에서 붙인다 (값이 없으면 "-") */
export function formatRate(rate: number | null): string {
  if (rate === null) return "-";
  return `${rate}%`;
}

/** 평균 소요 시간(초) → "1분 18초" (60초 미만은 "51초", 값이 없으면 "-") */
export function formatElapsedSeconds(seconds: number | null): string {
  if (seconds === null) return "-";
  const minutes = Math.floor(seconds / 60);
  const rest = seconds % 60;
  if (minutes === 0) return `${rest}초`;
  return `${minutes}분 ${rest}초`;
}
