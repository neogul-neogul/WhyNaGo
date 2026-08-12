import { apiFetch } from "@/lib/api";
import { refreshStreak } from "@/lib/streakStore";
import type {
  ChoiceGradingResponse,
  CreateEssaySolvedSessionRequest,
  CreateSolvedSessionRequest,
  CreateSolvedSessionResponse,
  EssayAnswerRequest,
  EssayAnswerResponse,
  EssaySessionResponse,
  PageResponse,
  QuestionCategory,
  QuestionDifficulty,
  QuestionResponse,
  QuestionTypeCode,
} from "@/types";

// 문제 풀이 도메인 API + 화면 라벨 매핑

/** 난이도 enum → 화면 라벨(하/중/상) */
export const DIFFICULTY_LABELS: Record<QuestionDifficulty, string> = {
  LOW: "하",
  MEDIUM: "중",
  HIGH: "상",
};

/** 카테고리 enum → 화면 라벨 */
export const CATEGORY_LABELS: Record<QuestionCategory, string> = {
  DB: "DB",
  NETWORK: "네트워크",
  ALGORITHM: "알고리즘",
  DATA_STRUCTURE: "자료구조",
  OS: "운영체제",
  DESIGN_PATTERN: "디자인패턴",
  LANGUAGE: "언어",
};

/** 유형 enum → 화면 라벨 */
export const TYPE_LABELS: Record<QuestionTypeCode, string> = {
  MULTIPLE_CHOICE: "객관식",
  ESSAY: "서술형",
};

function labelToEnum<T extends string>(labels: Record<T, string>, label: string): T | undefined {
  return (Object.keys(labels) as T[]).find((key) => labels[key] === label);
}

/** 화면 라벨(하/중/상) → 난이도 enum ("전체" 등 매핑 불가 라벨은 undefined) */
export function difficultyFromLabel(label: string): QuestionDifficulty | undefined {
  return labelToEnum(DIFFICULTY_LABELS, label);
}

/** 화면 라벨 → 카테고리 enum */
export function categoryFromLabel(label: string): QuestionCategory | undefined {
  return labelToEnum(CATEGORY_LABELS, label);
}

/** 화면 라벨(객관식/서술형) → 유형 enum */
export function typeFromLabel(label: string): QuestionTypeCode | undefined {
  return labelToEnum(TYPE_LABELS, label);
}

export interface QuestionFilters {
  type?: QuestionTypeCode;
  difficulty?: QuestionDifficulty;
  category?: QuestionCategory;
  /** 제목·지문 키워드 검색 */
  keyword?: string;
}

export interface QuestionPaging {
  /** 0부터 시작하는 페이지 번호 */
  page?: number;
  size?: number;
}

/** 문제은행 목록 한 페이지의 문항 수 */
export const QUESTION_PAGE_SIZE = 20;

/**
 * 문제은행 목록 조회 (본질문·꼬리질문 구분 없이 조건에 맞는 모든 문제. 서술형은 choices가 빈 배열).
 * 로그인 상태면 이미 푼 문제에 solved=true가 담겨 온다.
 */
export function fetchQuestions(
  filters: QuestionFilters = {},
  paging: QuestionPaging = {},
): Promise<PageResponse<QuestionResponse>> {
  const params = new URLSearchParams();
  if (filters.type) params.set("type", filters.type);
  if (filters.difficulty) params.set("difficulty", filters.difficulty);
  if (filters.category) params.set("category", filters.category);
  if (filters.keyword) params.set("q", filters.keyword);
  params.set("page", String(paging.page ?? 0));
  params.set("size", String(paging.size ?? QUESTION_PAGE_SIZE));
  return apiFetch<PageResponse<QuestionResponse>>(`/api/questions?${params.toString()}`);
}

/** 문제 단건 조회 — 목록이 페이지 단위라 상세 화면은 이 API로 문항을 가져온다 */
export function fetchQuestion(questionId: number): Promise<QuestionResponse> {
  return apiFetch<QuestionResponse>(`/api/questions/${questionId}`);
}

/** 보기 선택 결과(채점) 조회 — 채점 결과와 고른 보기의 꼬리질문을 함께 받는다 */
export function gradeQuestion(questionId: number, choiceId: number): Promise<ChoiceGradingResponse> {
  return apiFetch<ChoiceGradingResponse>(`/api/questions/${questionId}/choices/${choiceId}`);
}

/** 현재 시각을 백엔드 LocalDateTime 포맷("yyyy-MM-ddTHH:mm:ss")으로 변환 (세션 startedAt에 사용) */
export function nowAsLocalDateTime(): string {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
}

/** 풀이 세션 저장 (마지막 문항까지 답한 완료 세션만 저장) */
export async function saveSolvedSession(
  request: CreateSolvedSessionRequest,
): Promise<CreateSolvedSessionResponse> {
  const result = await apiFetch<CreateSolvedSessionResponse>("/api/solved-sessions", {
    method: "POST",
    body: request,
  });
  void refreshStreak();
  return result;
}

/** 서술형 세션 시작 — 이후 채점 요청을 묶을 대화 식별자를 발급받는다 */
export function startEssaySession(questionId: number): Promise<EssaySessionResponse> {
  return apiFetch<EssaySessionResponse>(`/api/questions/${questionId}/essay/sessions`, {
    method: "POST",
  });
}

/**
 * 서술형 답변 채점·꼬리질문 생성.
 * 이전 문답 맥락은 서버가 conversationId로 보관하므로 이번 턴의 질문·답변만 보낸다.
 */
export function evaluateEssayAnswer(
  questionId: number,
  request: EssayAnswerRequest,
): Promise<EssayAnswerResponse> {
  return apiFetch<EssayAnswerResponse>(`/api/questions/${questionId}/essay/answers`, {
    method: "POST",
    body: request,
  });
}

/** 서술형 풀이 세션 저장 (본질문 1 + 꼬리질문 2 문답 스냅샷. 완료 세션만 저장) */
export async function saveEssaySolvedSession(
  request: CreateEssaySolvedSessionRequest,
): Promise<CreateSolvedSessionResponse> {
  const result = await apiFetch<CreateSolvedSessionResponse>("/api/solved-sessions/essay", {
    method: "POST",
    body: request,
  });
  void refreshStreak();
  return result;
}
