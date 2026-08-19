import type {
  AdminDashboardAlert,
  AdminEmailBatch,
  AdminEmailRecipient,
  AdminInterviewRecord,
  AdminKpi,
  AdminMember,
  AdminMemberAnomaly,
  AdminMemberSignupMethod,
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

export const adminKpis: AdminKpi[] = [
  { label: "전체 회원 수", value: "1,204", unit: "명" },
  {
    label: "최근 7일 활동 회원 수",
    value: "312",
    unit: "명",
    delta: "▲ 6.1% (전주 294)",
    increased: true,
  },
  {
    label: "누적 풀이 수",
    value: "48,920",
    unit: "건",
    breakdown: "객관식 34,180 · 서술형 14,740",
  },
  {
    label: "오늘 면접 참여 / 완료",
    value: "412 / 323",
    delta: "▼ 5.3% (전일 435 / 340)",
    increased: false,
  },
  { label: "오늘 가입자 수", value: "37", unit: "명", delta: "▲ 27.6% (전일 29)", increased: true },
  { label: "오늘 풀이 수", value: "1,284", unit: "건", delta: "▲ 12.4% (전일 1,142)", increased: true },
  {
    label: "오늘 면접 참여자 수",
    value: "412",
    unit: "명",
    delta: "▼ 5.3% (전일 435)",
    increased: false,
  },
];

/** 대시보드 · 운영 알림 */
export const adminDashboardAlerts: AdminDashboardAlert[] = [
  {
    title: "메일 배치 미실행",
    detail: "오늘 21:00 메일 배치가 실행되지 않았습니다",
    ctaLabel: "메일 관리로 이동",
    ctaHref: "/admin/emails",
  },
  {
    title: "메일 배치 실패",
    detail:
      "실행 21:00 · 대상자 2,893건 · 성공 2,881 / 실패 12 · Invalid address 4, Mailbox full 3, Network error 5",
    ctaLabel: "메일 관리로 이동",
    ctaHref: "/admin/emails",
  },
  {
    title: "1일1면접 미고정",
    detail: "오늘 면접 문항이 고정되지 않았습니다",
    ctaLabel: "1일1면접 이력으로 이동",
    ctaHref: "/admin/interviews",
  },
  {
    title: "1일1면접 고정 실패",
    detail: "조건을 만족하는 후보 문항을 찾지 못했습니다 (NETWORK · MEDIUM)",
    ctaLabel: "1일1면접 이력으로 이동",
    ctaHref: "/admin/interviews",
  },
];

/** 대시보드 · AI 사용량 */
export const adminAiUsage: AdminKpi[] = [
  { label: "오늘 호출 수", value: "1,842", unit: "회" },
  { label: "최근 7일 호출 수", value: "11,306", unit: "회" },
  { label: "최근 7일 추정 비용", value: "$142.60" },
];

type MemberSeed = [
  string,
  string,
  string,
  string,
  ProgressTier,
  string,
  string,
  number,
  number,
  number,
  number,
  AdminMemberSignupMethod,
  AdminMemberAnomaly | null,
  AdminMemberStatus,
];

/** 회원 목록 헤더 요약 (총 인원 · 최근 7일 활동) */
export const adminMemberSummary = { total: 1204, activeWeek: 312 };

export const adminMembers: AdminMember[] = (
  [
    ["U20481", "devhoon", "dev****@gmail.com", "백엔드", "DIAMOND", "2025-11-02", "08-14 09:12", 18420, 62, 214, 1208, "Google", { label: "⚠ AI 과다", tone: "danger" }, "활성"],
    ["U20475", "mina_kim", "min****@naver.com", "프론트엔드", "GOLD", "2026-01-15", "08-14 08:40", 12180, 31, 142, 864, "일반", null, "활성"],
    ["U20460", "jaewon.dev", "jae****@kakao.com", "백엔드", "SILVER", "2026-03-08", "08-13 22:31", 8640, 12, 96, 551, "일반", { label: "⚠ 점수 불일치", tone: "warning" }, "정지"],
    ["U20431", "cs_master", "csm****@gmail.com", "데브옵스", "PLATINUM", "2025-09-21", "08-13 19:05", 15760, 48, 188, 1042, "Google", null, "활성"],
    ["U20402", "seoyeon", "seo****@naver.com", "안드로이드", "BRONZE", "2026-05-30", "08-12 11:44", 4120, 6, 52, 288, "일반", null, "활성"],
    ["U20388", "nodejs_lee", "nod****@gmail.com", "풀스택", "GOLD", "2026-02-11", "08-11 20:18", 11340, 27, 131, 742, "Google", { label: "⚠ AI 과다", tone: "danger" }, "활성"],
    ["U20350", "algo_park", "alg****@daum.net", "백엔드", "SILVER", "2026-04-19", "08-08 13:02", 7480, 9, 84, 470, "일반", null, "활성"],
    ["U20311", "yuna_os", "yun****@gmail.com", "iOS", "BRONZE", "2026-06-07", "08-02 08:57", 3260, 4, 41, 214, "일반", null, "탈퇴"],
    ["U20299", "hyunwoo.k", "hyu****@gmail.com", "백엔드", "SILVER", "2026-01-09", "08-01 12:30", 9140, 21, 132, 612, "일반", null, "활성"],
    ["U20287", "devsora", "dev****@naver.com", "프론트엔드", "GOLD", "2025-12-02", "07-30 19:44", 13020, 35, 151, 908, "Google", null, "활성"],
    ["U20265", "kim_ds", "kim****@kakao.com", "데이터", "BRONZE", "2026-06-21", "07-29 09:05", 2980, 3, 36, 182, "일반", null, "활성"],
    ["U20240", "leejh_dev", "lee****@gmail.com", "백엔드", "PLATINUM", "2025-10-14", "07-28 22:10", 16410, 52, 196, 1120, "일반", null, "활성"],
    ["U20233", "os_hunter", "osh****@daum.net", "데브옵스", "SILVER", "2026-02-27", "07-27 13:52", 8820, 14, 102, 588, "Google", null, "활성"],
    ["U20221", "frontnara", "fro****@naver.com", "프론트엔드", "GOLD", "2026-04-02", "07-26 11:19", 10460, 24, 118, 689, "일반", null, "활성"],
    ["U20208", "backend_yu", "bac****@gmail.com", "백엔드", "DIAMOND", "2025-08-19", "07-25 20:33", 19240, 71, 228, 1364, "일반", null, "활성"],
    ["U20194", "minseo.dev", "min****@kakao.com", "iOS", "BRONZE", "2026-05-11", "07-24 08:26", 3840, 5, 47, 246, "Google", null, "활성"],
    ["U20180", "querymaster", "que****@gmail.com", "데이터", "GOLD", "2026-03-19", "07-23 17:41", 11890, 29, 136, 798, "일반", null, "활성"],
    ["U20166", "android_ko", "and****@naver.com", "안드로이드", "SILVER", "2026-01-28", "07-22 10:07", 7960, 11, 91, 502, "일반", null, "활성"],
    ["U20151", "hoonsdev", "hoo****@gmail.com", "풀스택", "PLATINUM", "2025-11-25", "07-21 21:15", 15020, 44, 181, 1006, "Google", null, "활성"],
    ["U20139", "net_jiwon", "net****@daum.net", "백엔드", "BRONZE", "2026-07-03", "07-20 09:48", 2410, 2, 28, 146, "일반", null, "활성"],
    ["U20124", "sujin_c", "suj****@gmail.com", "프론트엔드", "GOLD", "2026-02-05", "07-19 14:22", 12640, 33, 147, 872, "일반", null, "활성"],
    ["U20110", "dbkim", "dbk****@naver.com", "데이터", "SILVER", "2025-12-17", "07-18 08:59", 9520, 17, 109, 634, "Google", null, "활성"],
    ["U20097", "taehodev", "tae****@kakao.com", "데브옵스", "BRONZE", "2026-06-30", "07-17 19:36", 3080, 3, 38, 196, "일반", null, "활성"],
    ["U20082", "algo_yeon", "alg****@gmail.com", "백엔드", "PLATINUM", "2025-09-08", "07-16 12:04", 17280, 57, 204, 1188, "일반", null, "활성"],
  ] satisfies MemberSeed[]
).map((m) => ({
  id: m[0],
  nickname: m[1],
  email: m[2],
  position: m[3],
  tier: m[4],
  joinedAt: m[5],
  lastVisitedAt: m[6],
  score: m[7],
  streakDays: m[8],
  interviewCount: m[9],
  solvedCount: m[10],
  signupMethod: m[11],
  anomaly: m[12] ?? undefined,
  status: m[13],
}));

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

type BatchSeed = [string, string, number, number, string | null];

/** 이메일 발송 배치 실행 이력 (최신순) */
export const adminEmailBatches: AdminEmailBatch[] = (
  [
    ["2026-08-17", "21:00", 2893, 12, "Invalid address (4건), Mailbox full (3건), Network error (5건)"],
    ["2026-08-16", "19:30", 2874, 0, null],
    ["2026-08-15", "18:15", 2860, 7, "Invalid address (3건), Mailbox full (4건)"],
    ["2026-08-14", "20:45", 2841, 0, null],
    ["2026-08-13", "21:10", 2836, 5, "Network error (5건)"],
    ["2026-08-12", "20:05", 2820, 0, null],
    ["2026-08-11", "19:50", 2811, 9, "Invalid address (6건), Mailbox full (3건)"],
    ["2026-08-10", "21:00", 2795, 0, null],
  ] satisfies BatchSeed[]
).map((b) => ({
  date: b[0],
  at: b[1],
  status: b[3] > 0 ? "일부 실패" : "정상",
  targetCount: b[2],
  successCount: b[2] - b[3],
  failCount: b[3],
  failureSummary: b[4] ?? undefined,
}));

const RECIPIENT_EMAILS = [
  "dev****@gmail.com",
  "min****@naver.com",
  "cs_****@kakao.com",
  "yun****@gmail.com",
  "bac****@daum.net",
  "jhy****@gmail.com",
  "seo****@outlook.com",
  "hae****@gmail.com",
];

const FAIL_REASONS = ["Invalid address", "Mailbox full", "Network error"];

/**
 * 배치별 개별 발송 목록 더미.
 * 실제로는 수천 건이지만 화면 확인용으로 한 페이지 분량만 만든다 (실패 건은 배치의 failCount에 맞춰 섞는다).
 */
export function adminEmailRecipients(batch: AdminEmailBatch): AdminEmailRecipient[] {
  const [hour, minute] = batch.at.split(":").map(Number);

  return RECIPIENT_EMAILS.map((email, i) => {
    const failed = batch.failCount > 0 && i % 3 === 1;
    const sentMinute = minute + Math.floor(i / 2);
    return {
      key: `${batch.date}-${i}`,
      email,
      sentAt: `${batch.date} ${String(hour).padStart(2, "0")}:${String(sentMinute).padStart(2, "0")}`,
      succeeded: !failed,
      reason: failed ? FAIL_REASONS[i % FAIL_REASONS.length] : "",
    };
  });
}

