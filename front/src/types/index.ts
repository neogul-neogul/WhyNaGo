// 공용 타입 정의

/** 사용자 직무 (백엔드 Position enum과 대응) */
export type Position = "BACKEND" | "FRONTEND" | "FULLSTACK";

/**
 * 사용자 권한 (백엔드 Role enum과 대응).
 * 로그인 응답과 access token에 함께 담기며, 승격은 운영 DB에서만 한다(docs/API.md 권한).
 */
export type Role = "USER" | "ADMIN";

/** 로그인한 사용자 정보 (백엔드 로그인 응답의 사용자 부분) */
export interface AuthUser {
  id: number;
  email: string;
  nickname: string;
  position: Position;
  role: Role;
}

/** 로그인 API 응답 바디 */
export interface LoginResponse extends AuthUser {
  accessToken: string;
  refreshToken: string;
}

/** 재발급 API 응답 바디 (사용자 정보는 포함하지 않는다) */
export interface ReissueResponse {
  accessToken: string;
  refreshToken: string;
}

/** 회원가입 API 응답 바디 */
export interface SignUpResponse {
  userId: number;
}

/** 내 프로필 조회 응답 — GET /api/users/me */
export interface UserProfileResponse {
  nickname: string;
  email: string;
  position: Position;
  /** 최소 학습 목표(하루 최소 풀이 세션 수) */
  dailyGoal: number;
}

/** 프로필 수정 요청 — PATCH /api/users/me (부분 수정이 아니라 전체 필드를 보낸다, 이메일은 수정 불가) */
export interface UpdateProfileRequest {
  nickname: string;
  position: Position;
  dailyGoal: number;
}

/** 상단 공통 헤더의 내비게이션 항목 */
export interface NavItem {
  key: string;
  label: string;
  href: string;
  /** "AI" 등 라벨 옆 배지 (없으면 미표시) */
  badge?: string;
  /** 탭 라벨 앞에 표시할 아이콘 (없으면 미표시) */
  icon?: "playlist";
}

/** 프로필 드롭다운 메뉴 아이콘 종류 */
export type ProfileMenuIcon =
  | "records"
  | "progress"
  | "weekly"
  | "user"
  | "settings"
  | "logout";

/** 프로필 드롭다운 메뉴 항목 */
export interface ProfileMenuItem {
  label: string;
  href: string;
  icon: ProfileMenuIcon;
  /** 로그아웃 등 강조(위험) 스타일 여부 */
  danger?: boolean;
  /** 단순 이동이 아닌 클라이언트 액션 (예: 로그아웃) */
  action?: "logout";
}

/** 로그인한 사용자 정보 */
export interface CurrentUser {
  name: string;
  role: string;
  email: string;
  /** 아바타에 표시할 한 글자 */
  initial: string;
}

/** 학습 통계 (연속/누적 학습일) */
export interface LearningStats {
  streakDays: number;
  cumulativeDays: number;
}

/** 오늘의 학습 목표 진행 상황 */
export interface TodayGoal {
  target: number;
  current: number;
  completed: boolean;
}

/** 오늘 지표 카드의 색상 톤 */
export type MetricTone = "default" | "warning" | "success";

/** 오늘 지표 카드 (푼 문제 / 면접 / 오답 복습) */
export interface TodayMetric {
  key: string;
  label: string;
  value: string;
  /** 값 뒤 단위 (예: "문제") */
  unit?: string;
  /** 값 뒤 부가 설명 (예: "· 4문제") */
  note?: string;
  tone: MetricTone;
}

/** 오늘 완료 가능한 학습 메뉴 카드 아이콘 종류 */
export type LearningMenuIcon = "solve" | "collections" | "wrong" | "interview";

/** 오늘 완료 가능한 학습 메뉴 카드 */
export interface LearningMenuItem {
  key: string;
  title: string;
  description: string;
  href: string;
  icon: LearningMenuIcon;
  /** 아이콘 배경색 */
  accentBg: string;
  /** 아이콘 선(stroke)색 */
  accentFg: string;
  badge?: string;
}

// ===== 문제 풀이 API (백엔드 question / solved-session 도메인) =====

/** 문제 카테고리 (백엔드 Category enum) */
export type QuestionCategory =
  | "DB"
  | "NETWORK"
  | "ALGORITHM"
  | "DATA_STRUCTURE"
  | "OS"
  | "DESIGN_PATTERN"
  | "LANGUAGE";

/** 문제 난이도 (백엔드 Difficulty enum) */
export type QuestionDifficulty = "LOW" | "MEDIUM" | "HIGH";

/** 문제 유형 (백엔드 QuestionType enum) */
export type QuestionTypeCode = "MULTIPLE_CHOICE" | "ESSAY";

/** 페이지 단위 목록 응답 공통 형식 (백엔드 PageResponse) */
export interface PageResponse<T> {
  content: T[];
  /** 0부터 시작하는 현재 페이지 번호 */
  page: number;
  size: number;
  /** 조건에 맞는 전체 항목 수 */
  totalElements: number;
  totalPages: number;
  last: boolean;
}

/** 객관식 선택지 (정답 여부는 서버만 알고, 채점 API로만 확인) */
export interface ChoiceResponse {
  id: number;
  content: string;
  sequence: number;
  /** 이 보기를 골랐을 때의 오답 해설 (정답 보기는 빈 값) */
  explanation: string | null;
  /** 이 보기 선택 시 이어질 꼬리질문 ID (없으면 세션 종료 지점) */
  relatedQuestionId: number | null;
}

/** 문제 조회 응답 (본질문 목록·꼬리질문 공용) */
export interface QuestionResponse {
  id: number;
  title: string;
  content: string;
  type: QuestionTypeCode;
  difficulty: QuestionDifficulty;
  category: QuestionCategory;
  explanation: string | null;
  choices: ChoiceResponse[];
  tags: string[];
  /** 이미 푼 문제인지 여부. 비로그인 조회와 꼬리질문 응답에서는 항상 false */
  solved: boolean;
}

// ===== 문제집 API (백엔드 problemset 도메인) — 유튜브 재생목록과 같은 개념, 항상 본인만 볼 수 있다 =====

/** 문제집 생성 요청 — 항상 빈 문제집으로 생성된다 (문제 담기는 별도 API) */
export interface CreateProblemSetRequest {
  name: string;
}

/** 문제집 생성 응답 */
export interface CreateProblemSetResponse {
  id: number;
  name: string;
  updatedAt: string;
}

/** 문제집 목록 조회 응답의 개별 항목 */
export interface ProblemSetSummaryResponse {
  id: number;
  name: string;
  itemCount: number;
  /** 담긴 문제 중 먼저 추가된 순서로 최대 3개의 제목 */
  previewTitles: string[];
  updatedAt: string;
}

/** 문제집 상세 조회 응답에 담긴 문제 (제목·카테고리·유형·난이도는 조회 시점에 서버가 조인해 채운다) */
export interface ProblemSetItemResponse {
  questionId: number;
  title: string;
  category: QuestionCategory;
  type: QuestionTypeCode;
  difficulty: QuestionDifficulty;
}

/** 문제집 상세 조회 응답 */
export interface ProblemSetDetailResponse {
  id: number;
  name: string;
  updatedAt: string;
  items: ProblemSetItemResponse[];
}

/** 문제집 저장 모달용 — 특정 문제 기준으로 내 문제집과 그 문제의 저장 여부 */
export interface ProblemSetMembershipResponse {
  id: number;
  name: string;
  itemCount: number;
  saved: boolean;
}

/** 보기 선택 결과(채점) 조회 응답 — GET /api/questions/{qid}/choices/{cid} */
export interface ChoiceGradingResponse {
  correct: boolean;
  /** 정답 보기 ID (하이라이트용) */
  correctChoiceId: number;
  /** 문제 전체(정답) 해설 */
  explanation: string | null;
  /** 고른 보기의 오답 해설 (정답이면 null) */
  choiceExplanation: string | null;
  /** 고른 보기에 연결된 꼬리질문 (없으면 null → 풀이 종료) */
  nextQuestion: QuestionResponse | null;
}

/** 세션 저장 요청의 문항 하나 */
export interface SolvedQuestionRequest {
  questionId: number;
  choiceId: number;
  /** 고른 보기의 relatedQuestionId (마지막 문항은 null) */
  relationQuestionId: number | null;
  /** 문항이 표시된 시점부터 "정답 확인"을 누른 시점까지의 초. 해설을 읽은 시간은 포함하지 않는다 */
  elapsedSeconds?: number;
}

/** 풀이 세션 저장 요청 — POST /api/solved-sessions */
export interface CreateSolvedSessionRequest {
  rootQuestion: SolvedQuestionRequest;
  followupQuestions: SolvedQuestionRequest[];
  /** 본질문을 처음 받은 시각(세션 시작 시각). 학습 기록의 소요시간 계산에 쓰인다 */
  startedAt: string;
}

/** 풀이 세션 저장 응답 (객관식·서술형 공용) */
export interface CreateSolvedSessionResponse {
  sessionId: number;
}

// ===== 서술형 풀이 API (백엔드 question / solvedsession 도메인) =====

/** 서술형 세션 시작 응답 — POST /api/questions/{qid}/essay/sessions */
export interface EssaySessionResponse {
  /** 서버가 발급한 대화 식별자. 이후 채점 요청에 담아 보낸다 */
  conversationId: string;
}

/** 서술형 답변 채점 요청 — POST /api/questions/{qid}/essay/answers */
export interface EssayAnswerRequest {
  conversationId: string;
  /** 이번에 채점할 문항 발문 (본질문 지문 또는 직전 응답의 nextFollowup.question) */
  question: string;
  answer: string;
}

/** 서술형 한 문항 채점 결과 */
export interface EssayGradingResponse {
  feedback: string;
  modelAnswer: string;
  /** 통과 여부 (LLM 점수를 서버가 임계값으로 환산한 값) */
  isCorrect: boolean;
}

/** AI가 생성한 다음 꼬리질문 */
export interface EssayFollowupResponse {
  question: string;
}

/** 서술형 답변 채점 응답 */
export interface EssayAnswerResponse {
  grading: EssayGradingResponse;
  /** 마지막 문항(3턴째)이면 null → 면접 종료 */
  nextFollowup: EssayFollowupResponse | null;
}

/** 서술형 세션 저장 요청의 문항 하나 (문답 스냅샷) */
export interface EssaySolvedQuestionRequest {
  /** 본질문만 값. 꼬리질문은 재사용 가능한 Question이 없어 null */
  questionId: number | null;
  questionText: string;
  userAnswer: string;
  feedback: string;
  modelAnswer: string;
  /** 채점 API가 산출한 통과 여부를 그대로 전달 (저장 시 재채점하지 않음) */
  isCorrect: boolean;
}

/** 서술형 풀이 세션 저장 요청 — POST /api/solved-sessions/essay */
export interface CreateEssaySolvedSessionRequest {
  rootQuestion: EssaySolvedQuestionRequest;
  /** 꼬리질문 스냅샷 목록. 정확히 2개 (본질문 1 + 꼬리질문 2 = 3문항 고정) */
  followupQuestions: EssaySolvedQuestionRequest[];
  /** 본질문을 처음 받은 시각(세션 시작 시각). 학습 기록의 소요시간 계산에 쓰인다 */
  startedAt: string;
}

// ===== 1일 1면접 API (백엔드 interview 도메인) =====
// 문답 흐름은 서술형과 같아 채점 응답 타입(EssayGradingResponse·EssayFollowupResponse)을 재사용한다.
// 다른 점: conversationId·startedAt·본질문 ID를 서버가 소유하므로 요청에 담지 않는다 (docs/API.md Interview API).

/** 오늘의 면접 상태. AVAILABLE은 DB 상태가 아니라 "오늘 면접 행이 없음"을 뜻한다 */
export type InterviewStatusCode = "AVAILABLE" | "IN_PROGRESS" | "COMPLETED";

/** 문항 유형 (본질문/꼬리질문) */
export type InterviewItemType = "MAIN" | "FOLLOWUP";

/** 오늘의 면접 상태 조회 응답 — GET /api/interviews/today */
export interface TodayInterviewResponse {
  status: InterviewStatusCode;
  /** AVAILABLE이면 null */
  interviewId: number | null;
}

/** 오늘의 면접 질문 (그날 모든 사용자에게 동일) */
export interface InterviewQuestionResponse {
  id: number;
  title: string;
  content: string;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
}

/** 면접 시작 응답 — POST /api/interviews (요청 본문 없음: 질문은 서버가 정한다) */
export interface StartInterviewResponse {
  interviewId: number;
  question: InterviewQuestionResponse;
  /** 총 문항 수 (본질문 + 꼬리질문 2) */
  totalQuestionCount: number;
  /** 문항당 제한 시간(초). 서버는 강제하지 않으며 화면이 표시·강제한다 */
  timeLimitSeconds: number;
  startedAt: string;
}

/** 면접 답변 채점 요청 — POST /api/interviews/{id}/answers (conversationId는 서버가 소유) */
export interface InterviewAnswerRequest {
  question: string;
  /** 제한 시간 내 미작성을 인정해 빈 문자열을 허용한다 */
  answer: string;
}

/** 면접 답변 채점 응답 (서술형과 동일 형태) */
export interface InterviewAnswerResponse {
  grading: EssayGradingResponse;
  /** 마지막 문항(3턴째)이면 null → 면접 종료 */
  nextFollowup: EssayFollowupResponse | null;
}

/** 완료 요청의 문항 하나 (문답 스냅샷). 본질문 ID는 서버가 채우므로 담지 않는다 */
export interface InterviewAnswerSnapshotRequest {
  questionText: string;
  userAnswer: string;
  feedback: string;
  modelAnswer: string;
  isCorrect: boolean;
}

/** 면접 완료 요청 — POST /api/interviews/{id}/complete */
export interface CompleteInterviewRequest {
  rootQuestion: InterviewAnswerSnapshotRequest;
  /** 정확히 2개 */
  followupQuestions: InterviewAnswerSnapshotRequest[];
  /** 화면 이탈 횟수 (클라이언트 집계값) */
  focusLossCount: number;
}

/** 면접 완료 응답 */
export interface CompleteInterviewResponse {
  interviewId: number;
  /** 생성된 풀이 세션 ID (학습 기록·오답노트가 이 세션으로 잡힌다) */
  solvedSessionId: number;
}

/** 면접 결과의 문항 하나 */
export interface InterviewResultItemResponse {
  sequence: number;
  type: InterviewItemType;
  questionText: string;
  userAnswer: string;
  feedback: string;
  modelAnswer: string;
  isCorrect: boolean;
}

/** 면접 결과 조회 응답 — GET /api/interviews/{id} (완료된 면접만) */
export interface InterviewResultResponse {
  interviewId: number;
  interviewDate: string;
  status: InterviewStatusCode;
  category: QuestionCategory;
  totalCount: number;
  correctCount: number;
  focusLossCount: number;
  startedAt: string;
  completedAt: string;
  /** 소요 시간(초). 서버가 제한 시간을 강제하지 않아 180을 넘을 수 있다 */
  durationSeconds: number;
  items: InterviewResultItemResponse[];
}

/** 면접 기록 목록 항목 — GET /api/interviews (완료된 면접 전부, 정답/오답 필터링 없음) */
export interface InterviewHistoryResponse {
  interviewId: number;
  interviewDate: string;
  category: QuestionCategory;
  title: string;
  totalCount: number;
  correctCount: number;
  completedAt: string;
}

// ===== 오답노트 API (백엔드 wrongnote 도메인) =====
// 오답노트는 상태·반복 횟수·출처를 두지 않는다 (docs/DOMAIN.md 결정 사항) — 목록 필터는 북마크 여부뿐.

/** 오답노트 목록 항목 — GET /api/wrong-notes */
export interface WrongNoteSummaryResponse {
  id: number;
  questionId: number;
  type: QuestionTypeCode;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  /** 본 질문 제목 */
  title: string;
  isBookmarked: boolean;
  solvedAt: string;
}

/** 오답노트 상세의 객관식 보기 */
export interface WrongNoteChoiceResponse {
  id: number;
  content: string;
  sequence: number;
  isCorrect: boolean;
}

/** 오답노트 상세의 객관식 문항 (본질문/꼬리질문 각 1개) */
export interface WrongNoteMultipleChoiceItemResponse {
  sequence: number;
  questionId: number;
  title: string;
  content: string;
  choices: WrongNoteChoiceResponse[];
  userChoiceId: number;
  correctChoiceId: number;
  isCorrect: boolean;
  explanation: string;
  /** 내가 고른 보기의 오답 해설. 정답이면 null */
  choiceExplanation: string | null;
}

/** 오답노트 상세의 서술형 문항 (본질문/꼬리질문 각 1개) */
export interface WrongNoteEssayItemResponse {
  sequence: number;
  questionText: string;
  userAnswer: string;
  feedback: string;
  modelAnswer: string;
  isCorrect: boolean;
}

/** 오답노트 상세 — GET /api/wrong-notes/{id}. 유형에 따라 둘 중 하나만 채워진다 */
export interface WrongNoteDetailResponse {
  id: number;
  type: QuestionTypeCode;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  isBookmarked: boolean;
  solvedAt: string;
  multipleChoiceItems: WrongNoteMultipleChoiceItemResponse[] | null;
  essayItems: WrongNoteEssayItemResponse[] | null;
}

/** 오답노트 북마크 변경 응답 — PATCH /api/wrong-notes/{id}/bookmark */
export interface WrongNoteBookmarkResponse {
  isBookmarked: boolean;
}

/** 모의진단 카테고리별 통계 */
export interface CatStat {
  name: string;
  acc: number;
  count: number;
  grade: string;
  change: string;
}

/** 카테고리별 성장 곡선 데이터 */
export interface GrowthDatum {
  cat: string;
  /** 주차별 등급 (A~D) */
  grades: string[];
}

/** 잔디 한 칸 (날짜별 등급은 프런트에서 daily-counts를 변환해 만든다) */
export interface GrassDay {
  date: string;
  level: number;
  color: string;
  count: number;
}

// ===== 학습 기록 API (백엔드 learningrecord 도메인) =====
// method(진입 경로)·잔디 등급(0~4단계)·"학습량 점수"는 백엔드에 없다 (docs/DOMAIN.md 보류) — lib/records.ts에서 프런트가 계산한다.

/** 최근 학습 기록 항목 — GET /api/learning-records/recent */
export interface RecentRecordResponse {
  sessionId: number;
  type: QuestionTypeCode;
  /** 본질문의 카테고리 */
  category: QuestionCategory;
  totalCount: number;
  correctCount: number;
  wrongCount: number;
  startedAt: string | null;
  solvedAt: string;
}

/** 연속·누적 학습일 — GET /api/learning-records/streak (KST 자정 기준) */
export interface StreakResponse {
  streakDays: number;
  cumulativeDays: number;
}

/** 일자별 학습량 — GET /api/learning-records/daily-counts. 학습이 없었던 날짜는 응답에 없다 */
export interface DailyRecordCountResponse {
  date: string;
  sessionCount: number;
  questionCount: number;
}

// ===== 진척도 API (백엔드 progress 도메인) =====

/** 티어 (백엔드 Tier enum) */
export type ProgressTier = "BRONZE" | "SILVER" | "GOLD" | "PLATINUM" | "DIAMOND";

/** 티어 구간 하한. 진행 바의 구간 비율을 그리는 데 쓴다 */
export interface TierRangeResponse {
  tier: ProgressTier;
  minScore: number;
}

/** 카테고리 한 칸의 진척도. 기록이 없는 카테고리도 0으로 채워 항상 전부 내려온다 */
export interface CategoryProgressResponse {
  category: QuestionCategory;
  /** 문제은행에 있는 그 카테고리 전체 문항 수 */
  totalCount: number;
  /** 풀어본 문항 수 (정답/오답 무관) */
  solvedCount: number;
  /** 점수를 받은(세션 전부 정답) 문항 수 */
  correctCount: number;
  score: number;
}

/** 점수·티어·카테고리별 현황 — GET /api/progress */
export interface ProgressResponse {
  score: number;
  tier: ProgressTier;
  nextTier: ProgressTier | null;
  scoreToNextTier: number;
  totalQuestionCount: number;
  categories: CategoryProgressResponse[];
  tiers: TierRangeResponse[];
  maxScore: number;
}

/** 진척도 상단 통계 — GET /api/progress/summary */
export interface ProgressSummaryResponse {
  cumulativeDays: number;
  streakDays: number;
  totalQuestionCount: number;
  totalCorrectCount: number;
  totalWrongCount: number;
  completedInterviewCount: number;
}

/** 진척도 지표 카드 */
export interface ProgressMetric {
  label: string;
  value: string;
  unit: string;
  color: string;
}

/** 주간 리포트 카드 */
export interface WeeklyCard {
  label: string;
  value: string;
  delta: string;
  deltaColor: string;
}

/** 마이페이지 프로필 */
export interface Profile {
  nickname: string;
  email: string;
  job: string;
  goal: string;
}

// ===== 알림 설정 API (백엔드 notification 도메인) =====
// 설정 저장·조회만 다룬다. 실제 알림 발송(스케줄러·이메일)은 백엔드에 아직 없다.

/** 내 알림 설정 조회 응답 — GET /api/notification-settings/me */
export interface NotificationSettingResponse {
  everyDayRemind: boolean;
}

/** 알림 설정 수정 요청 — PATCH /api/notification-settings/me (부분 수정이 아니라 전체 필드를 보낸다) */
export type UpdateNotificationSettingRequest = NotificationSettingResponse;

// ===== 관리자 API (백엔드 admin 도메인) =====
// 관리자 전용 경로(/api/admin)라 role=ADMIN이 아니면 403이다.
// 사용자용 문제 응답과 달리 선택지의 정답 여부(correct)를 그대로 내려준다.

/** 관리자 문제 목록 행 — GET /api/admin/questions */
export interface AdminQuestionResponse {
  id: number;
  title: string;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  type: QuestionTypeCode;
  /** 전체 풀이 응답 수 (본질문·꼬리질문 구분 없이 합산) */
  solveCount: number;
  /** 정답률(%). 아직 아무도 풀지 않았으면 0이 아니라 null */
  correctRate: number | null;
}

/** 관리자 문제 상세의 선택지 — 사용자용 ChoiceResponse와 달리 정답 여부를 노출한다 */
export interface AdminChoiceResponse {
  id: number;
  sequence: number;
  content: string;
  correct: boolean;
  /** 이 보기를 골랐을 때의 오답 해설 (정답 보기는 빈 값) */
  explanation: string | null;
  /** 이 보기 선택 시 이어질 꼬리질문 ID (없으면 세션 종료 지점) */
  relatedQuestionId: number | null;
}

/** 관리자 문제 상세 — GET /api/admin/questions/{questionId} */
export interface AdminQuestionDetailResponse {
  id: number;
  title: string;
  content: string;
  type: QuestionTypeCode;
  difficulty: QuestionDifficulty;
  category: QuestionCategory;
  explanation: string | null;
  /** 서술형은 항상 빈 배열 */
  choices: AdminChoiceResponse[];
  tags: string[];
  /** 서술형만 값이 있다. 객관식은 지표가 더 많은 통계 API를 쓰므로 항상 null */
  solveCount: number | null;
  correctRate: number | null;
}

/** 문제 통계 · 보기별 선택 분포 */
export interface ChoiceDistributionResponse {
  choiceId: number;
  sequence: number;
  content: string;
  correct: boolean;
  selectedCount: number;
  /** 전체 응답 대비 비율(%) */
  selectedRate: number;
}

/** 객관식 문제 통계 — GET /api/admin/questions/{questionId}/statistics (서술형에는 쓸 수 없다) */
export interface MultipleChoiceStatisticsResponse {
  questionId: number;
  totalSolveCount: number;
  correctCount: number;
  correctRate: number;
  /** 평균 소요 시간(초). 수집된 응답이 없으면 0이 아니라 null */
  averageElapsedSeconds: number | null;
  /** 위 평균이 몇 건으로 계산됐는지. totalSolveCount보다 작을 수 있다 */
  elapsedSampleCount: number;
  /** 응답이 한 건도 없으면 null */
  mostChosenChoice: ChoiceDistributionResponse | null;
  /** 현재 보기 전체가 sequence 오름차순. 아무도 고르지 않은 보기도 0건으로 포함된다 */
  choiceDistribution: ChoiceDistributionResponse[];
}

/** 전일/전주 대비 비교가 필요한 지표. 증감률·화살표는 서버가 만들지 않으므로 lib/admin.ts에서 조립한다 */
export interface MetricComparisonResponse {
  current: number;
  previous: number;
}

/** 누적 풀이 수 — 세션 수가 아니라 문항 수다 */
export interface CumulativeSolveCountResponse {
  total: number;
  multipleChoiceCount: number;
  essayCount: number;
}

/** 오늘 면접 참여(시작)·완료 수 */
export interface InterviewMetricResponse {
  started: MetricComparisonResponse;
  completed: MetricComparisonResponse;
}

/** 대시보드 운영 알림 — 표시 문구·CTA 링크는 프런트가 type으로 결정한다 */
export interface DashboardAlertResponse {
  type: DashboardAlertCode;
  /** DAILY_INTERVIEW_NOT_PINNED의 대상 일자 (YYYY-MM-DD) */
  interviewDate: string | null;
}

/** 서버가 판정하는 알림 종류. 메일 배치·고정 실패 알림은 아직 근거 데이터가 없어 내려오지 않는다 */
export type DashboardAlertCode = "DAILY_INTERVIEW_NOT_PINNED";

/** 관리자 대시보드 — GET /api/admin/dashboard */
export interface DashboardResponse {
  totalMemberCount: number;
  /** 최근 7일(오늘 포함) 풀이한 회원 수 · previous는 직전 7일 */
  activeMember7Days: MetricComparisonResponse;
  cumulativeSolveCount: CumulativeSolveCountResponse;
  todaySolveCount: MetricComparisonResponse;
  todaySignUpCount: MetricComparisonResponse;
  todayInterview: InterviewMetricResponse;
  /** 해당 사항이 없으면 빈 배열 */
  alerts: DashboardAlertResponse[];
}

/** 회원 가입 경로 (백엔드 AuthProvider enum과 대응) */
export type AuthProviderCode = "LOCAL" | "GOOGLE";

/** GET /api/admin/members 의 한 행 */
export interface AdminMemberResponse {
  id: number;
  nickname: string;
  /** 마스킹하지 않은 원본. 표시용 마스킹은 화면이 한다 */
  email: string;
  position: Position;
  provider: AuthProviderCode;
  /** 가입 시각 추적 이전에 가입한 회원은 null */
  createdAt: string | null;
}

/** GET /api/admin/members/summary */
export interface AdminMemberSummaryResponse {
  totalCount: number;
  activeWeekCount: number;
}

/** GET /api/admin/members/{userId} — 목록 필드 + 학습 지표 */
export interface AdminMemberDetailResponse extends AdminMemberResponse {
  streakDays: number;
  solvedQuestionCount: number;
  completedInterviewCount: number;
}

/** 배치 실행 상태 — 실패 0건 SUCCESS, 성공·실패 혼재 PARTIAL_FAILURE, 성공 0건 FAILED */
export type EmailBatchStatus = "SUCCESS" | "PARTIAL_FAILURE" | "FAILED";

export type EmailSendStatus = "SUCCESS" | "FAILURE";

/** GET /api/admin/email-batches — content 원소 */
export interface EmailBatchExecutionResponse {
  id: number;
  /** 배치가 시작된 시각 ("2026-08-19T21:00:00") */
  executedAt: string;
  totalTargetCount: number;
  successCount: number;
  failureCount: number;
  status: EmailBatchStatus;
}

/** 실패 사유별 건수 — 발송 상세가 페이징되므로 서버가 전체를 집계해 내려준다 */
export interface FailureReasonResponse {
  reason: string;
  count: number;
}

/** GET /api/admin/email-batches/{executionId} — 목록 필드 + 실패 사유 요약 */
export interface EmailBatchExecutionDetailResponse extends EmailBatchExecutionResponse {
  failureReasons: FailureReasonResponse[];
}

/** GET /api/admin/email-batches/{executionId}/send-logs — content 원소 */
export interface EmailSendLogResponse {
  id: number;
  userId: number;
  /** 발송 시점 주소 스냅샷. 대상 조회 자체가 실패했다면 null이다 */
  recipientEmail: string | null;
  sentAt: string;
  status: EmailSendStatus;
  failureReason: string | null;
}

// ===== 대시보드 화면 조립용 =====
// 서버 응답이 아니라 화면이 그릴 형태다. 응답의 숫자를 표시 문자열로 바꾼 결과가 담긴다.

/** 대시보드 KPI 카드 */
export interface AdminKpi {
  label: string;
  value: string;
  unit?: string;
  /** 전일/전주 대비 증감 (없으면 표시하지 않음) */
  delta?: string;
  increased?: boolean;
  /** 값 아래 보조 내역 (예: "객관식 34,180 · 서술형 14,740") */
  breakdown?: string;
}

/** 대시보드 알림 카드 */
export interface AdminDashboardAlert {
  title: string;
  detail: string;
  ctaLabel: string;
  ctaHref: string;
}

// ===== 관리자 화면 (더미 전용) =====
// 문제 목록·상세를 뺀 나머지 어드민 화면은 백엔드 API가 아직 없어 화면만 유지한다.
// 아래 타입은 전부 src/mocks/admin.ts의 더미용이며, 백엔드 스펙이 나오면 위 API 응답 타입들처럼 서버 스펙 그대로 다시 정의한다.

/**
 * 회원 목록·상세에서 API가 내려주지 않는 값.
 * 점수·티어는 조회 시점 파생값이라 컬럼이 없고, 최근활동일·이상징후·상태는 판정할 데이터가 없다.
 * 회원 ID로 고정 배정해 목록과 모달이 같은 값을 본다 (→ mocks/admin.ts의 mockMemberMeta).
 */
export interface AdminMemberMeta {
  tier: ProgressTier;
  score: number;
  lastVisitedAt: string;
  anomaly?: AdminMemberAnomaly;
  status: AdminMemberStatus;
}

/** 회원 이상징후 태그 */
export interface AdminMemberAnomaly {
  label: string;
  tone: "danger" | "warning";
}

/** 회원 상태 */
export type AdminMemberStatus = "활성" | "정지" | "탈퇴";

/** 문제 공개 상태 — Question에 컬럼이 없어 화면에서 목업으로 채운다 */
export type AdminQuestionStatus = "PUBLISHED" | "DRAFT" | "ARCHIVED";

/** 문제 수정 폼 상태 (객관식은 explanation·answerIndex·options, 서술형은 modelAnswer를 쓴다) */
export interface AdminQuestionForm {
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  tags: string[];
  title: string;
  body: string;
  explanation?: string;
  answerIndex?: number;
  options?: AdminQuestionFormOption[];
  /** 서술형 문제의 모범답안 */
  modelAnswer?: string;
}

export interface AdminQuestionFormOption {
  text: string;
  explanation: string;
}

/** 1일1면접 출제 이력 행 */
export interface AdminInterviewRecord {
  date: string;
  body: string;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  participants: number;
  completionRate: string;
  within3MinRate: string;
  avgScore: string;
  questionId: string;
}

