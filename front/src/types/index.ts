// 공용 타입 정의

/** 사용자 직무 (백엔드 Position enum과 대응) */
export type Position = "BACKEND" | "FRONTEND" | "FULLSTACK";

/** 로그인한 사용자 정보 (백엔드 로그인 응답의 사용자 부분) */
export interface AuthUser {
  id: number;
  email: string;
  nickname: string;
  position: Position;
}

/** 로그인 API 응답 바디 */
export interface LoginResponse extends AuthUser {
  accessToken: string;
  refreshToken: string;
}

/** 회원가입 API 응답 바디 */
export interface SignUpResponse {
  userId: number;
}

/** 상단 공통 헤더의 내비게이션 항목 */
export interface NavItem {
  key: string;
  label: string;
  href: string;
  /** "AI" 등 라벨 옆 배지 (없으면 미표시) */
  badge?: string;
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
export type LearningMenuIcon = "solve" | "wrong" | "interview" | "mock";

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

// ===== 학습 도메인 (문제/오답/면접/진단/기록) =====

/** 객관식 꼬리질문 */
export interface MultipleChoiceFollowup {
  text: string;
  options: string[];
  /** 정답 보기 인덱스 */
  answer: number;
  explanation: string;
  /** 보기별 오답 해설 (정답 보기는 빈 문자열) */
  optExp?: string[];
}

/** 객관식 문제 (본 질문 + 꼬리질문) */
export interface MultipleChoiceQuestion {
  cat: string;
  diff: string;
  text: string;
  options: string[];
  answer: number;
  explanation: string;
  optExp: string[];
  tags: string[];
  followups: MultipleChoiceFollowup[];
}

/** 서술형 문제 (AI 면접식 꼬리질문) */
export interface EssayQuestion {
  cat: string;
  diff: string;
  text: string;
  /** 모범답안 */
  model: string;
  keywords: string[];
  /** 꼬리질문 목록 */
  followups: string[];
  /** 본 질문 + 꼬리질문별 피드백 */
  feedbacks: string[];
  /** 꼬리질문별 모범답안 */
  followupModels: string[];
}

/** 문제은행 항목 */
export interface Problem {
  type: "객관식" | "서술형";
  /** 객관식이면 multipleChoiceQuestions[], 서술형이면 essayQuestions[]의 인덱스 */
  qi: number;
  title: string;
  cat: string;
  keywords: string[];
  diff: string;
  /** 완료한 사람 수 */
  solved: number;
  /** 정답률(%) */
  rate: number;
  status: "완료" | "오답" | "안 푼 문제";
}

/** 오답노트 꼬리질문 */
export interface WrongFollowup {
  text: string;
  options: string[];
  answer: number;
  myAnswer: number;
  explanation: string;
  wrongExp: string;
}

/** 오답노트 항목 */
export interface WrongNote {
  q: string;
  cat: string;
  diff: string;
  status: "미복습" | "복습 중" | "반복 오답" | "해결 완료";
  repeat: number;
  source: string;
  solvedAt: string;
  options: string[];
  myAnswer: number;
  correctAnswer: number;
  explanation: string;
  wrongExp: string;
  followups: WrongFollowup[];
}

/** 1일 1면접 문항 (카테고리별) */
export interface InterviewItem {
  q: string;
  feedback: string;
  followup: string;
  improved: string;
  keywords: string[];
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

/** 학습 기록 항목 */
export interface RecordItem {
  date: string;
  time: string;
  method: string;
  cats: string[];
  solved: number;
  correct: number;
  wrong: number;
  score: number;
}

/** 잔디 한 칸 */
export interface GrassDay {
  level: number;
  color: string;
  count: number;
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
  bio: string;
}
