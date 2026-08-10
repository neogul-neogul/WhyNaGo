import { apiFetch } from "@/lib/api";
import type {
  CompleteInterviewRequest,
  CompleteInterviewResponse,
  InterviewAnswerRequest,
  InterviewAnswerResponse,
  InterviewResultResponse,
  StartInterviewResponse,
  TodayInterviewResponse,
} from "@/types";

// 1일 1면접 도메인 API.
// 날짜·오늘의 질문·대화 식별자·시작 시각을 모두 서버가 소유하므로 요청에 담지 않는다.

/** 서버가 timeLimitSeconds를 주지 않는 경우에만 쓰는 문항당 제한 시간(초) */
export const INTERVIEW_TIME_LIMIT_FALLBACK = 180;

/** 총 문항 수 폴백 (본질문 + 꼬리질문 2) */
export const INTERVIEW_TOTAL_QUESTION_FALLBACK = 3;

/** 이탈 감지 오탐을 거르는 디바운스(ms) — 이 안에 복귀하면 카운트하지 않는다 */
export const FOCUS_LOSS_DEBOUNCE_MS = 300;

/** AI 분당 한도에 걸렸을 때의 재시도 쿨다운(초) */
export const QUOTA_COOLDOWN_SECONDS = 60;

/** 면접 에러 코드 */
export const INTERVIEW_ALREADY_STARTED_TODAY = "INTERVIEW_ALREADY_STARTED_TODAY";
export const INTERVIEW_NOT_FOUND = "INTERVIEW_NOT_FOUND";
export const INTERVIEW_NOT_COMPLETED = "INTERVIEW_NOT_COMPLETED";
export const INTERVIEW_NOT_IN_PROGRESS = "INTERVIEW_NOT_IN_PROGRESS";
export const INTERVIEW_NOT_CANCELABLE = "INTERVIEW_NOT_CANCELABLE";
export const INTERVIEW_QUESTION_NOT_AVAILABLE = "INTERVIEW_QUESTION_NOT_AVAILABLE";

/** AI 채점 에러 코드 (서술형과 공유) */
export const ESSAY_AI_QUOTA_EXCEEDED = "ESSAY_AI_QUOTA_EXCEEDED";
export const ESSAY_AI_DAILY_QUOTA_EXCEEDED = "ESSAY_AI_DAILY_QUOTA_EXCEEDED";

/** 오늘 면접을 볼 수 있는지 조회 (날짜는 서버가 KST로 판단) */
export function fetchTodayInterview(): Promise<TodayInterviewResponse> {
  return apiFetch<TodayInterviewResponse>("/api/interviews/today");
}

/** 오늘의 면접 시작. 호출 순간 오늘 자리가 소진된다 */
export function startInterview(): Promise<StartInterviewResponse> {
  return apiFetch<StartInterviewResponse>("/api/interviews", { method: "POST" });
}

/** 면접 한 턴 채점·꼬리질문 생성 (아무것도 저장하지 않는다) */
export function answerInterview(
  interviewId: number,
  request: InterviewAnswerRequest,
): Promise<InterviewAnswerResponse> {
  return apiFetch<InterviewAnswerResponse>(`/api/interviews/${interviewId}/answers`, {
    method: "POST",
    body: request,
  });
}

/** 면접 완료 — 3문항 문답을 학습 기록으로 저장하고 면접을 종료한다 */
export function completeInterview(
  interviewId: number,
  request: CompleteInterviewRequest,
): Promise<CompleteInterviewResponse> {
  return apiFetch<CompleteInterviewResponse>(`/api/interviews/${interviewId}/complete`, {
    method: "POST",
    body: request,
  });
}

/** 면접 취소 — 한 문항도 채점받지 못했을 때만 성공한다 (오늘 자리 반환) */
export function cancelInterview(interviewId: number): Promise<void> {
  return apiFetch<void>(`/api/interviews/${interviewId}`, { method: "DELETE" });
}

/** 완료된 면접의 결과 조회 */
export function fetchInterviewResult(interviewId: number): Promise<InterviewResultResponse> {
  return apiFetch<InterviewResultResponse>(`/api/interviews/${interviewId}`);
}

/** 문항 순번(0-based) → 화면 라벨 */
export function interviewItemLabel(index: number): string {
  return index === 0 ? "본 질문" : `꼬리질문 ${index}`;
}

/** 남은 초 → "m:ss" (타이머 표시) */
export function formatRemaining(seconds: number): string {
  const safe = Math.max(0, seconds);
  return `${Math.floor(safe / 60)}:${String(safe % 60).padStart(2, "0")}`;
}

/** 소요 초 → "3분 12초" (결과 요약 표시) */
export function formatDurationSeconds(seconds: number): string {
  const minutes = Math.floor(seconds / 60);
  const rest = seconds % 60;
  return minutes > 0 ? `${minutes}분 ${rest}초` : `${rest}초`;
}

/** ISO LocalDate("2026-08-07") → "2026.08.07" */
export function formatInterviewDate(interviewDate: string): string {
  return interviewDate.replaceAll("-", ".");
}
