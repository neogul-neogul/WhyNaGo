import type {
  AdminInterviewRecord,
  AdminKpi,
  AdminMemberAnomaly,
  AdminMemberMeta,
  AdminMemberStatus,
  AdminQuestionStatus,
  ProgressTier,
  QuestionCategory,
  QuestionDifficulty,
} from "@/types";

// 관리자 화면 더미 데이터. 어드민 백엔드 API가 없어 화면 확인용으로만 쓴다.
// 카테고리·난이도 목록은 더미가 아니라 실제 도메인 enum이라 lib/admin.ts에 둔다.

export const ADMIN_TIERS: ProgressTier[] = [
  "DIAMOND",
  "PLATINUM",
  "GOLD",
  "SILVER",
  "BRONZE",
];

// KPI·운영 알림은 GET /api/admin/dashboard로 연동됐다 (조립은 lib/admin.ts).
// 메일 배치 알림은 배치 실행 이력이, 1일1면접 고정 실패 알림은 실패 기록이 없어 아직 서버가 판정하지 못한다.

/** 대시보드 · AI 사용량 — 호출 로깅이 없어 서버가 내려주지 않는다 */
export const adminAiUsage: AdminKpi[] = [
  { label: "오늘 호출 수", value: "1,842", unit: "회" },
  { label: "최근 7일 호출 수", value: "11,306", unit: "회" },
  { label: "최근 7일 추정 비용", value: "$142.60" },
];

// 점수·티어는 조회 시점 파생값이라 컬럼이 없고(서버가 정렬·필터도 못 한다),
// 최근활동일·이상징후·상태는 판정할 데이터 자체가 없다. 화면은 유지하되 아래 값으로 채운다.
const MOCK_MEMBER_TIERS: ProgressTier[] = ["DIAMOND", "GOLD", "SILVER", "PLATINUM", "BRONZE"];

const MOCK_MEMBER_SCORES = [18420, 12180, 8640, 15760, 4120];

const MOCK_MEMBER_VISITED_DATES = [
  "08-14 09:12",
  "08-13 22:31",
  "08-12 11:44",
  "08-11 20:18",
  "08-08 13:02",
];

const MOCK_MEMBER_ANOMALIES: (AdminMemberAnomaly | null)[] = [
  null,
  null,
  { label: "⚠ AI 과다", tone: "danger" },
  null,
  { label: "⚠ 점수 불일치", tone: "warning" },
];

const MOCK_MEMBER_STATUSES: AdminMemberStatus[] = ["활성", "활성", "활성", "정지", "탈퇴"];

/** 백엔드가 내려주지 않는 티어·점수·최근활동일·이상징후·상태 목업 (회원 ID가 같으면 항상 같은 값) */
export function mockMemberMeta(userId: number): AdminMemberMeta {
  return {
    tier: MOCK_MEMBER_TIERS[userId % MOCK_MEMBER_TIERS.length],
    score: MOCK_MEMBER_SCORES[userId % MOCK_MEMBER_SCORES.length],
    lastVisitedAt: MOCK_MEMBER_VISITED_DATES[userId % MOCK_MEMBER_VISITED_DATES.length],
    anomaly: MOCK_MEMBER_ANOMALIES[userId % MOCK_MEMBER_ANOMALIES.length] ?? undefined,
    status: MOCK_MEMBER_STATUSES[userId % MOCK_MEMBER_STATUSES.length],
  };
}

// 문제의 공개 상태·수정일은 Question에 컬럼이 없어 API가 내려주지 않는다.
// 화면은 유지하되 값만 문제 ID로 고정 배정해 채운다 (컬럼·수정 API 설계는 별도 계획).
const MOCK_QUESTION_STATUSES: AdminQuestionStatus[] = [
  "PUBLISHED",
  "PUBLISHED",
  "DRAFT",
  "PUBLISHED",
  "ARCHIVED",
];

const MOCK_QUESTION_CREATED_DATES = ["2025-11-08", "2025-10-02", "2026-01-15", "2025-12-01", "2026-03-19"];

const MOCK_QUESTION_UPDATED_DATES = [
  "2026-08-12 14:22",
  "2026-08-11 09:40",
  "2026-08-09 18:05",
  "2026-08-05 11:31",
  "2026-07-28 16:12",
];

const MOCK_QUESTION_EDITORS = ["김도현", "이서연", "박지훈"];

/** 백엔드에 없는 목록·상세의 상태·등록일·수정일·수정자 목업 (문제 ID가 같으면 항상 같은 값) */
export function mockQuestionMeta(questionId: number): {
  status: AdminQuestionStatus;
  createdAt: string;
  updatedAt: string;
  updatedBy: string;
} {
  return {
    status: MOCK_QUESTION_STATUSES[questionId % MOCK_QUESTION_STATUSES.length],
    createdAt: MOCK_QUESTION_CREATED_DATES[questionId % MOCK_QUESTION_CREATED_DATES.length],
    updatedAt: MOCK_QUESTION_UPDATED_DATES[questionId % MOCK_QUESTION_UPDATED_DATES.length],
    updatedBy: MOCK_QUESTION_EDITORS[questionId % MOCK_QUESTION_EDITORS.length],
  };
}

type InterviewSeed = [string, string, QuestionCategory, QuestionDifficulty, number, string, string, string, string];

export const adminInterviews: AdminInterviewRecord[] = (
  [
    ["08-14", "TCP의 혼잡 제어 알고리즘이 동작하는 과정을 슬로우 스타트부터 설명하세요.", "NETWORK", "MEDIUM", 412, "78.4%", "61.2%", "64.2", "Q1042"],
    ["08-13", "인덱스가 있는 컬럼에 함수를 적용하면 인덱스를 타지 못하는 이유를 서술하세요.", "DB", "HIGH", 438, "71.0%", "48.6%", "57.8", "Q0987"],
    ["08-12", "프로세스와 스레드의 차이를 메모리 구조 관점에서 설명하세요.", "OS", "LOW", 455, "88.1%", "79.4%", "81.5", "Q0961"],
    ["08-11", "HTTP와 HTTPS의 차이에 대한 설명으로 옳지 않은 것은?", "NETWORK", "LOW", 401, "91.5%", "86.0%", "88.3", "Q1009"],
    ["08-10", "싱글톤 패턴을 멀티스레드 환경에서 안전하게 구현하는 방법은?", "DESIGN_PATTERN", "MEDIUM", 387, "69.2%", "44.1%", "61.0", "Q0975"],
    ["08-09", "퀵 정렬의 최악 시간 복잡도가 발생하는 조건은?", "ALGORITHM", "MEDIUM", 429, "83.7%", "72.5%", "76.4", "Q0955"],
    ["08-08", "이진 탐색 트리와 힙의 구조적 차이로 옳은 것은?", "DATA_STRUCTURE", "LOW", 444, "90.3%", "81.8%", "84.9", "Q0981"],
    ["08-07", "캐시 지역성의 원리를 시간·공간 관점에서 설명하세요.", "OS", "MEDIUM", 396, "74.5%", "52.8%", "66.1", "Q0961"],
    ["08-06", "다익스트라 알고리즘의 전제 조건으로 옳은 것은?", "ALGORITHM", "LOW", 421, "89.7%", "78.4%", "85.2", "Q0910"],
    ["08-05", "TCP 3-way handshake 과정에서 SYN flooding이 발생하는 원리와 방어 기법을 서술하세요.", "NETWORK", "HIGH", 383, "66.3%", "41.2%", "58.4", "Q1042"],
    ["08-04", "정규화 과정에서 제3정규형의 조건으로 옳은 것은?", "DB", "MEDIUM", 410, "80.2%", "65.7%", "74.8", "Q0942"],
    ["08-03", "자바의 String과 StringBuilder 차이로 옳지 않은 것은?", "LANGUAGE", "LOW", 436, "91.1%", "84.2%", "87.6", "Q0968"],
    ["08-02", "HTTP와 HTTPS의 차이에 대한 설명으로 옳지 않은 것은?", "NETWORK", "LOW", 418, "90.4%", "83.1%", "86.9", "Q1009"],
    ["08-01", "교착 상태의 네 가지 필요 조건에 해당하지 않는 것은?", "OS", "LOW", 402, "88.6%", "80.5%", "84.1", "Q0923"],
    ["07-31", "해시 충돌 처리 방식의 트레이드오프를 서술하세요.", "DATA_STRUCTURE", "HIGH", 371, "63.9%", "38.8%", "55.2", "Q0936"],
    ["07-30", "페이지 교체 알고리즘 중 벨라디 예외가 발생하는 것은?", "OS", "MEDIUM", 389, "78.9%", "60.4%", "72.3", "Q1005"],
    ["07-29", "퀵 정렬의 최악 시간 복잡도가 발생하는 조건은?", "ALGORITHM", "MEDIUM", 415, "82.4%", "69.1%", "75.8", "Q0955"],
    ["07-28", "인덱스가 있는 컬럼에 함수를 적용하면 인덱스를 타지 못하는 이유를 서술하세요.", "DB", "HIGH", 428, "70.2%", "47.3%", "57.1", "Q0987"],
    ["07-27", "파이썬의 GIL이 성능에 영향을 주는 상황으로 옳은 것은?", "LANGUAGE", "MEDIUM", 394, "76.5%", "58.2%", "68.4", "Q0930"],
    ["07-26", "이진 탐색 트리와 힙의 구조적 차이로 옳은 것은?", "DATA_STRUCTURE", "LOW", 440, "89.8%", "80.9%", "83.7", "Q0981"],
    ["07-25", "전략 패턴과 상태 패턴의 구조적 차이를 실제 예시와 함께 서술하세요.", "DESIGN_PATTERN", "HIGH", 366, "62.7%", "36.5%", "54.3", "Q1021"],
    ["07-24", "OSI 7계층에서 라우터가 동작하는 계층은?", "NETWORK", "LOW", 425, "92.0%", "85.4%", "88.1", "Q0949"],
    ["07-23", "다이나믹 프로그래밍과 그리디의 적용 조건 차이를 서술하세요.", "ALGORITHM", "HIGH", 358, "61.4%", "35.2%", "53.6", "Q1001"],
    ["07-22", "싱글톤 패턴을 멀티스레드 환경에서 안전하게 구현하는 방법은?", "DESIGN_PATTERN", "MEDIUM", 381, "68.8%", "44.6%", "60.7", "Q0975"],
    ["07-21", "다음 중 트랜잭션의 격리 수준 REPEATABLE READ에서 발생할 수 있는 이상 현상은?", "DB", "MEDIUM", 407, "79.5%", "63.8%", "71.2", "Q1041"],
    ["07-20", "병합 정렬의 시간 복잡도와 공간 복잡도를 올바르게 짝지은 것은?", "ALGORITHM", "LOW", 431, "87.3%", "76.8%", "82.5", "Q1038"],
    ["07-19", "컨텍스트 스위칭이 발생하는 시점과 그 비용을 줄이기 위한 방법을 서술하세요.", "OS", "MEDIUM", 399, "72.1%", "49.5%", "63.4", "Q1033"],
    ["07-18", "자바의 가비지 컬렉션 대상이 되는 객체 판별 기준으로 옳은 것은?", "LANGUAGE", "MEDIUM", 412, "77.8%", "59.9%", "70.6", "Q1014"],
  ] satisfies InterviewSeed[]
).map((r) => ({
  date: r[0],
  body: r[1],
  category: r[2],
  difficulty: r[3],
  participants: r[4],
  completionRate: r[5],
  within3MinRate: r[6],
  avgScore: r[7],
  questionId: r[8],
}));

/** 1일1면접 이력 · 운영 경고 배너 */
export const adminInterviewAlerts: { tone: "warning" | "danger"; title: string; detail: string; mono: boolean }[] = [
  {
    tone: "warning",
    title: "최근 14일 중복 출제 2건",
    detail: "Q1042 (08-14 · 08-05), Q1009 (08-11 · 08-02)",
    mono: true,
  },
  {
    tone: "danger",
    title: "카테고리 편중: NETWORK 5회",
    detail: "최근 14일 기준 · 랜덤 선정 가중치 점검 필요",
    mono: false,
  },
];

/** 1일1면접 이력 · 최근 30일 카테고리별 출제 횟수 */
export const adminCategoryBars: { label: string; value: number }[] = [
  { label: "NETWORK", value: 7 },
  { label: "DB", value: 5 },
  { label: "OS", value: 4 },
  { label: "ALGORITHM", value: 3 },
  { label: "DATA_STRUCT", value: 3 },
  { label: "LANGUAGE", value: 2 },
  { label: "DESIGN_PT", value: 1 },
];

