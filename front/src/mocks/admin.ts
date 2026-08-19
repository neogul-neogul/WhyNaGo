import type {
  AdminChoiceDistribution,
  AdminEmailLog,
  AdminInterviewRecord,
  AdminKpi,
  AdminMember,
  AdminQuestion,
  AdminQuestionDetail,
  AdminQuestionForm,
  AdminQuestionSession,
  AdminSolveRecord,
  AdminSolveResult,
  ProgressTier,
  QuestionCategory,
  QuestionDifficulty,
} from "@/types";

// 관리자 화면 더미 데이터. 어드민 백엔드 API가 없어 화면 확인용으로만 쓴다.

/** 어드민 화면에서 그대로 노출하는 카테고리 코드 목록 */
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

export const ADMIN_TIERS: ProgressTier[] = [
  "DIAMOND",
  "PLATINUM",
  "GOLD",
  "SILVER",
  "BRONZE",
];

export const adminKpis: AdminKpi[] = [
  { label: "오늘 가입자 수", value: "37", unit: "명", delta: "▲ 8 (전일 29)", increased: true },
  { label: "오늘 풀이 수", value: "1,284", unit: "건", delta: "▲ 142 (전일 1,142)", increased: true },
  { label: "오늘 면접 참여자 수", value: "412", unit: "명", delta: "▼ 23 (전일 435)", increased: false },
  { label: "스트릭 유지 사용자 수", value: "1,038", unit: "명", delta: "▲ 24 (전일 1,014)", increased: true },
];

/** 대시보드 · 오늘의 1일1면접 요약 */
export const adminTodayInterview = {
  date: "2026-08-17",
  category: "NETWORK" as QuestionCategory,
  difficulty: "MEDIUM" as QuestionDifficulty,
  body: "TCP의 혼잡 제어 알고리즘이 동작하는 과정을 슬로우 스타트부터 설명하고, 빠른 회복이 필요한 이유를 서술하세요.",
  participants: "412",
  completionRate: "78.4%",
  avgScore: "64.2",
};

/** 대시보드 · 일별 풀이 수 추이 (최근 30일 중 표시 구간) */
export const adminDailySolveCounts: number[] = [
  540, 600, 650, 565, 805, 860, 890, 775, 935, 1005, 895, 1055, 1100, 1170, 1035, 1230, 1290, 1385,
];

/** 대시보드 추이 차트 x축 라벨 (라벨, 데이터 인덱스) */
export const adminSolveChartLabels: { label: string; index: number }[] = [
  { label: "07-19", index: 0 },
  { label: "07-27", index: 5 },
  { label: "08-04", index: 10 },
  { label: "08-11", index: 14 },
  { label: "08-17", index: 17 },
];

type MemberSeed = [string, string, string, string, ProgressTier, string, string, number, number, number, number];

export const adminMembers: AdminMember[] = (
  [
    ["U20481", "devhoon", "dev****@gmail.com", "백엔드", "DIAMOND", "2025-11-02", "08-14 09:12", 18420, 62, 214, 1208],
    ["U20475", "mina_kim", "min****@naver.com", "프론트엔드", "GOLD", "2026-01-15", "08-14 08:40", 12180, 31, 142, 864],
    ["U20460", "jaewon.dev", "jae****@kakao.com", "백엔드", "SILVER", "2026-03-08", "08-13 22:31", 8640, 12, 96, 551],
    ["U20431", "cs_master", "csm****@gmail.com", "데브옵스", "PLATINUM", "2025-09-21", "08-13 19:05", 15760, 48, 188, 1042],
    ["U20402", "seoyeon", "seo****@naver.com", "안드로이드", "BRONZE", "2026-05-30", "08-12 11:44", 4120, 6, 52, 288],
    ["U20388", "nodejs_lee", "nod****@gmail.com", "풀스택", "GOLD", "2026-02-11", "08-11 20:18", 11340, 27, 131, 742],
    ["U20350", "algo_park", "alg****@daum.net", "백엔드", "SILVER", "2026-04-19", "08-08 13:02", 7480, 9, 84, 470],
    ["U20311", "yuna_os", "yun****@gmail.com", "iOS", "BRONZE", "2026-06-07", "08-02 08:57", 3260, 4, 41, 214],
    ["U20299", "hyunwoo.k", "hyu****@gmail.com", "백엔드", "SILVER", "2026-01-09", "08-01 12:30", 9140, 21, 132, 612],
    ["U20287", "devsora", "dev****@naver.com", "프론트엔드", "GOLD", "2025-12-02", "07-30 19:44", 13020, 35, 151, 908],
    ["U20265", "kim_ds", "kim****@kakao.com", "데이터", "BRONZE", "2026-06-21", "07-29 09:05", 2980, 3, 36, 182],
    ["U20240", "leejh_dev", "lee****@gmail.com", "백엔드", "PLATINUM", "2025-10-14", "07-28 22:10", 16410, 52, 196, 1120],
    ["U20233", "os_hunter", "osh****@daum.net", "데브옵스", "SILVER", "2026-02-27", "07-27 13:52", 8820, 14, 102, 588],
    ["U20221", "frontnara", "fro****@naver.com", "프론트엔드", "GOLD", "2026-04-02", "07-26 11:19", 10460, 24, 118, 689],
    ["U20208", "backend_yu", "bac****@gmail.com", "백엔드", "DIAMOND", "2025-08-19", "07-25 20:33", 19240, 71, 228, 1364],
    ["U20194", "minseo.dev", "min****@kakao.com", "iOS", "BRONZE", "2026-05-11", "07-24 08:26", 3840, 5, 47, 246],
    ["U20180", "querymaster", "que****@gmail.com", "데이터", "GOLD", "2026-03-19", "07-23 17:41", 11890, 29, 136, 798],
    ["U20166", "android_ko", "and****@naver.com", "안드로이드", "SILVER", "2026-01-28", "07-22 10:07", 7960, 11, 91, 502],
    ["U20151", "hoonsdev", "hoo****@gmail.com", "풀스택", "PLATINUM", "2025-11-25", "07-21 21:15", 15020, 44, 181, 1006],
    ["U20139", "net_jiwon", "net****@daum.net", "백엔드", "BRONZE", "2026-07-03", "07-20 09:48", 2410, 2, 28, 146],
    ["U20124", "sujin_c", "suj****@gmail.com", "프론트엔드", "GOLD", "2026-02-05", "07-19 14:22", 12640, 33, 147, 872],
    ["U20110", "dbkim", "dbk****@naver.com", "데이터", "SILVER", "2025-12-17", "07-18 08:59", 9520, 17, 109, 634],
    ["U20097", "taehodev", "tae****@kakao.com", "데브옵스", "BRONZE", "2026-06-30", "07-17 19:36", 3080, 3, 38, 196],
    ["U20082", "algo_yeon", "alg****@gmail.com", "백엔드", "PLATINUM", "2025-09-08", "07-16 12:04", 17280, 57, 204, 1188],
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
  cumulativeDays: m[9],
  solvedCount: m[10],
}));

type QuestionSeed = [string, QuestionCategory, QuestionDifficulty, "객관식" | "서술형", string, number, string, string];

export const adminQuestions: AdminQuestion[] = (
  [
    ["Q1042", "NETWORK", "HIGH", "서술형", "TCP 3-way handshake 과정에서 SYN flooding이 발생하는 원리와 방어 기법을 서술하세요.", 312, "41.2%", "08-12"],
    ["Q1041", "DB", "MEDIUM", "객관식", "다음 중 트랜잭션의 격리 수준 REPEATABLE READ에서 발생할 수 있는 이상 현상은?", 588, "63.8%", "08-12"],
    ["Q1038", "ALGORITHM", "LOW", "객관식", "병합 정렬의 시간 복잡도와 공간 복잡도를 올바르게 짝지은 것은?", 1204, "82.5%", "08-11"],
    ["Q1033", "OS", "MEDIUM", "서술형", "컨텍스트 스위칭이 발생하는 시점과 그 비용을 줄이기 위한 방법을 서술하세요.", 476, "55.1%", "08-10"],
    ["Q1029", "DATA_STRUCTURE", "LOW", "객관식", "해시 테이블에서 체이닝과 개방 주소법의 차이로 옳지 않은 것은?", 942, "7.4%", "08-09"],
    ["Q1021", "DESIGN_PATTERN", "HIGH", "서술형", "전략 패턴과 상태 패턴의 구조적 차이를 실제 예시와 함께 서술하세요.", 205, "38.9%", "08-07"],
    ["Q1014", "LANGUAGE", "MEDIUM", "객관식", "자바의 가비지 컬렉션 대상이 되는 객체 판별 기준으로 옳은 것은?", 803, "71.6%", "08-05"],
    ["Q1009", "NETWORK", "LOW", "객관식", "HTTP와 HTTPS의 차이에 대한 설명으로 옳지 않은 것은?", 1562, "88.3%", "08-02"],
    ["Q1005", "OS", "MEDIUM", "객관식", "페이지 교체 알고리즘 중 벨라디 예외가 발생하는 것은?", 1118, "74.2%", "07-31"],
    ["Q1001", "ALGORITHM", "HIGH", "서술형", "다이나믹 프로그래밍과 그리디의 적용 조건 차이를 서술하세요.", 288, "42.7%", "07-30"],
    ["Q0994", "NETWORK", "MEDIUM", "객관식", "TCP 흐름 제어와 혼잡 제어의 차이로 옳은 것은?", 1033, "68.9%", "07-28"],
    ["Q0987", "DB", "HIGH", "서술형", "인덱스가 있는 컬럼에 함수를 적용하면 인덱스를 타지 못하는 이유를 서술하세요.", 341, "39.5%", "07-27"],
    ["Q0981", "DATA_STRUCTURE", "MEDIUM", "객관식", "이진 탐색 트리와 힙의 구조적 차이로 옳은 것은?", 874, "66.1%", "07-25"],
    ["Q0975", "DESIGN_PATTERN", "MEDIUM", "객관식", "싱글톤 패턴을 멀티스레드 환경에서 안전하게 구현하는 방법은?", 512, "57.3%", "07-24"],
    ["Q0968", "LANGUAGE", "LOW", "객관식", "자바의 String과 StringBuilder 차이로 옳지 않은 것은?", 1340, "84.6%", "07-22"],
    ["Q0961", "OS", "HIGH", "서술형", "프로세스와 스레드의 차이를 메모리 구조 관점에서 설명하세요.", 397, "45.8%", "07-21"],
    ["Q0955", "ALGORITHM", "MEDIUM", "객관식", "퀵 정렬의 최악 시간 복잡도가 발생하는 조건은?", 968, "71.4%", "07-19"],
    ["Q0949", "NETWORK", "LOW", "객관식", "OSI 7계층에서 라우터가 동작하는 계층은?", 1712, "90.2%", "07-18"],
    ["Q0942", "DB", "MEDIUM", "객관식", "정규화 과정에서 제3정규형의 조건으로 옳은 것은?", 786, "62.5%", "07-16"],
    ["Q0936", "DATA_STRUCTURE", "HIGH", "서술형", "해시 충돌 처리 방식의 트레이드오프를 서술하세요.", 254, "37.1%", "07-15"],
    ["Q0930", "LANGUAGE", "MEDIUM", "객관식", "파이썬의 GIL이 성능에 영향을 주는 상황으로 옳은 것은?", 690, "59.8%", "07-13"],
    ["Q0923", "OS", "LOW", "객관식", "교착 상태의 네 가지 필요 조건에 해당하지 않는 것은?", 1455, "86.9%", "07-12"],
    ["Q0917", "DESIGN_PATTERN", "HIGH", "서술형", "전략 패턴을 적용해 조건 분기를 제거하는 과정을 서술하세요.", 231, "36.4%", "07-10"],
    ["Q0910", "ALGORITHM", "LOW", "객관식", "다익스트라 알고리즘의 전제 조건으로 옳은 것은?", 1204, "81.3%", "07-09"],
  ] satisfies QuestionSeed[]
).map((q) => ({
  id: q[0],
  category: q[1],
  difficulty: q[2],
  type: q[3],
  title: q[4],
  solveCount: q[5],
  correctRate: q[6],
  updatedAt: q[7],
}));

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

type EmailSeed = [string, string, boolean, string];

export const adminEmailLogs: AdminEmailLog[] = (
  [
    ["08-17 07:00", "devhoon · dev****@gmail.com", true, ""],
    ["08-17 07:00", "mina_kim · min****@naver.com", false, "존재하지 않는 주소 (550 5.1.1)"],
    ["08-17 07:00", "jaewon.dev · jae****@kakao.com", true, ""],
    ["08-17 07:00", "cs_master · csm****@gmail.com", false, "메일함 용량 초과 (552)"],
    ["08-17 07:00", "seoyeon · seo****@naver.com", true, ""],
    ["08-17 07:00", "nodejs_lee · nod****@gmail.com", false, "일시적 서버 오류 (421)"],
    ["08-16 07:00", "algo_park · alg****@daum.net", true, ""],
    ["08-16 07:00", "yuna_os · yun****@gmail.com", false, "수신 거부 처리됨"],
    ["08-16 07:00", "hyunwoo.k · hyu****@gmail.com", true, ""],
    ["08-16 07:00", "devsora · dev****@naver.com", false, "스팸 정책 차단 (554)"],
    ["08-16 07:00", "kim_ds · kim****@kakao.com", true, ""],
    ["08-15 07:00", "leejh_dev · lee****@gmail.com", false, "존재하지 않는 주소 (550 5.1.1)"],
    ["08-15 07:00", "os_hunter · osh****@daum.net", true, ""],
    ["08-15 07:00", "frontnara · fro****@naver.com", false, "일시적 서버 오류 (421)"],
    ["08-15 07:00", "backend_yu · bac****@gmail.com", true, ""],
    ["08-15 07:00", "minseo.dev · min****@kakao.com", false, "메일함 용량 초과 (552)"],
    ["08-14 07:00", "querymaster · que****@gmail.com", true, ""],
    ["08-14 07:00", "android_ko · and****@naver.com", false, "수신 거부 처리됨"],
    ["08-14 07:00", "hoonsdev · hoo****@gmail.com", true, ""],
    ["08-14 07:00", "net_jiwon · net****@daum.net", false, "스팸 정책 차단 (554)"],
    ["08-13 07:00", "sujin_c · suj****@gmail.com", true, ""],
    ["08-13 07:00", "dbkim · dbk****@naver.com", false, "존재하지 않는 주소 (550 5.1.1)"],
    ["08-13 07:00", "taehodev · tae****@kakao.com", true, ""],
    ["08-13 07:00", "algo_yeon · alg****@gmail.com", false, "일시적 서버 오류 (421)"],
  ] satisfies EmailSeed[]
).map((e, i) => ({ key: `E${i}`, at: e[0], to: e[1], succeeded: e[2], reason: e[3] }));

/** 문제 상세 더미 (시안에 상세가 준비된 문제만 존재한다) */
export const adminQuestionDetails: Record<string, AdminQuestionDetail> = {
  Q1041: {
    title: "REPEATABLE READ의 이상 현상",
    body: "트랜잭션 격리 수준을 REPEATABLE READ로 설정했을 때, 여전히 발생할 수 있는 이상 현상으로 가장 적절한 것은?",
    options: [
      { text: "Dirty Read — 커밋되지 않은 데이터를 읽는 현상", correct: false },
      { text: "Phantom Read — 범위 조회 시 없던 행이 나타나는 현상", correct: true },
      { text: "Lost Update — 동시 갱신으로 한쪽 변경이 사라지는 현상", correct: false },
      { text: "Non-Repeatable Read — 같은 행을 재조회 시 값이 달라지는 현상", correct: false },
    ],
    answerExplanation:
      "REPEATABLE READ는 동일 트랜잭션 내 같은 행의 재조회 결과를 보장하지만, 조회 범위에 새로 커밋된 행이 추가되는 것은 막지 못한다. 따라서 범위 조회에서 없던 행이 나타나는 Phantom Read가 발생할 수 있다.",
    wrongExplanations: [
      { number: 1, text: "Dirty Read는 READ UNCOMMITTED에서만 발생하며, REPEATABLE READ에서는 커밋된 데이터만 읽는다." },
      { number: 3, text: "Lost Update는 격리 수준이 아니라 동시 갱신 제어(잠금·낙관적 락)의 문제다." },
      { number: 4, text: "Non-Repeatable Read는 REPEATABLE READ에서 MVCC 스냅샷으로 방지된다." },
    ],
    tags: ["#트랜잭션", "#격리수준", "#MVCC"],
    createdAt: "2025-11-08",
    updatedAt: "2026-08-12 14:22",
    responseCount: 1842,
    correctRate: "63.8%",
    avgSpent: "1분 18초",
    topPick: "2번",
    topPickRate: "63.8%",
    distribution: [
      { label: "보기 1", text: "Dirty Read — 커밋되지 않은 데이터를 읽는 현상", count: 318, rate: "17.3%", correct: false },
      { label: "✓ 정답", text: "Phantom Read — 범위 조회 시 없던 행이 나타나는 현상", count: 1175, rate: "63.8%", correct: true },
      { label: "보기 3", text: "Lost Update — 동시 갱신으로 한쪽 변경이 사라지는 현상", count: 214, rate: "11.6%", correct: false },
      { label: "보기 4", text: "Non-Repeatable Read — 재조회 시 값이 달라지는 현상", count: 135, rate: "7.3%", correct: false },
    ] satisfies AdminChoiceDistribution[],
    sessions: [
      { user: "devhoon", pick: "2 · Phantom Read — 범위 조회 시 없던 행이 나타나는 현상", correct: true, spent: "0분 51초", at: "08-14 09:24" },
      { user: "mina_kim", pick: "4 · Non-Repeatable Read — 재조회 시 값이 달라지는 현상", correct: false, spent: "1분 33초", at: "08-14 08:47" },
      { user: "cs_master", pick: "2 · Phantom Read — 범위 조회 시 없던 행이 나타나는 현상", correct: true, spent: "0분 38초", at: "08-13 23:02" },
      { user: "jaewon.dev", pick: "1 · Dirty Read — 커밋되지 않은 데이터를 읽는 현상", correct: false, spent: "2분 07초", at: "08-13 20:15" },
      { user: "nodejs_lee", pick: "2 · Phantom Read — 범위 조회 시 없던 행이 나타나는 현상", correct: true, spent: "1분 12초", at: "08-13 12:41" },
      { user: "algo_park", pick: "3 · Lost Update — 동시 갱신으로 한쪽 변경이 사라지는 현상", correct: false, spent: "0분 44초", at: "08-12 19:58" },
      { user: "seoyeon", pick: "2 · Phantom Read — 범위 조회 시 없던 행이 나타나는 현상", correct: true, spent: "1분 49초", at: "08-12 11:20" },
    ] satisfies AdminQuestionSession[],
  },
  Q1042: {
    title: "SYN flooding이 성립하는 원인",
    body: "TCP 3-way handshake 과정에서 SYN flooding 공격이 성립하는 원인과 서버 측 방어 기법을 서술하세요.",
    answerExplanation:
      "서버가 SYN을 받은 뒤 SYN+ACK를 보내고 ACK를 기다리는 동안 연결 정보를 백로그 큐에 유지하기 때문에, 위조된 SYN을 대량으로 보내면 큐가 고갈되어 정상 연결을 받을 수 없다. SYN 쿠키, 백로그 확장, 타임아웃 축소로 완화한다.",
    tags: ["#TCP", "#핸드셰이크", "#보안"],
    createdAt: "2025-10-02",
    updatedAt: "2026-08-12 11:05",
  },
};

/** 문제 수정 화면 초기 폼 (시안 기준) */
export const adminQuestionFormSeed: AdminQuestionForm = {
  category: "NETWORK",
  difficulty: "HIGH",
  tags: ["#TCP", "#핸드셰이크", "#보안"],
  title: "SYN flooding이 성립하는 원인",
  body: "다음 중 TCP 3-way handshake 과정에서 SYN flooding 공격이 성립하는 원인으로 가장 적절한 것은?",
  explanation:
    "서버가 SYN을 받은 뒤 SYN+ACK를 보내고 ACK를 기다리는 동안 연결 정보를 백로그 큐에 유지하기 때문에…",
  answerIndex: 0,
  options: [
    { text: "서버가 ACK를 기다리며 백로그 큐에 연결 상태를 유지하기 때문", explanation: "" },
    {
      text: "TCP가 UDP보다 헤더 크기가 커서 대역폭을 많이 쓰기 때문",
      explanation: "헤더 크기는 공격 성립과 무관합니다. SYN flooding은 연결 상태 자원 소모를 노린 공격입니다.",
    },
    {
      text: "3-way handshake가 UDP 기반으로 동작하기 때문",
      explanation: "handshake는 TCP 고유 절차입니다. 프로토콜 전제부터 잘못되었습니다.",
    },
    {
      text: "서버가 SYN 패킷을 검증 없이 즉시 폐기하기 때문",
      explanation: "즉시 폐기한다면 자원 소모가 없어 공격이 성립하지 않습니다.",
    },
  ],
};

/** 문제 상세 화면에서 쓰는, 목록 행 + 상세 더미를 합친 형태 */
export interface AdminQuestionView extends AdminQuestion {
  detail: AdminQuestionDetail;
  /** 통계 탭 상단 지표 카드 */
  statCards: { label: string; value: string; unit: string }[];
}

/** 목록 정보와 상세 더미를 합쳐 문제 상세 화면용 데이터를 만든다 (상세가 없는 문제는 목록 값으로 채운다) */
export function adminQuestionView(id: string): AdminQuestionView | null {
  const question = adminQuestions.find((q) => q.id === id);
  if (!question) return null;

  const seed = adminQuestionDetails[id];
  const detail: AdminQuestionDetail = {
    title: seed?.title ?? question.title,
    body: seed?.body ?? question.title,
    options: seed?.options,
    answerExplanation: seed?.answerExplanation,
    wrongExplanations: seed?.wrongExplanations,
    tags: seed?.tags ?? [`#${question.category.toLowerCase()}`],
    createdAt: seed?.createdAt ?? "2025-12-01",
    updatedAt: seed?.updatedAt ?? `2026-${question.updatedAt} 10:00`,
    responseCount: seed?.responseCount ?? question.solveCount,
    correctRate: seed?.correctRate ?? question.correctRate,
    avgSpent: seed?.avgSpent ?? "1분 42초",
    topPick: seed?.topPick,
    topPickRate: seed?.topPickRate,
    distribution: seed?.distribution,
    sessions: seed?.sessions,
  };

  return {
    ...question,
    detail,
    statCards: [
      { label: "전체 풀이 횟수", value: (detail.responseCount ?? 0).toLocaleString(), unit: "회" },
      { label: "정답률", value: detail.correctRate ?? "—", unit: "" },
      { label: "평균 소요 시간", value: detail.avgSpent ?? "—", unit: "" },
      { label: "가장 많이 고른 선택지", value: detail.topPick ?? "—", unit: detail.topPickRate ?? "" },
    ],
  };
}

const SOLVE_DATES = [
  "08-14 09:15", "08-13 21:02", "08-13 08:31", "08-12 22:47", "08-11 07:58",
  "08-10 20:12", "08-09 08:44", "08-08 22:19", "08-07 07:36", "08-06 21:53",
];

const SOLVE_SPENTS = [
  "2분 41초", "0분 48초", "3분 05초", "1분 12초", "2분 58초",
  "1분 34초", "0분 57초", "2분 16초", "1분 41초", "3분 22초",
];

const SOLVE_RESULTS: Record<string, AdminSolveResult[]> = {
  "풀이 이력": ["완료", "오답", "완료", "완료", "복습", "완료", "오답", "완료", "완료", "복습"],
  오답노트: ["오답", "오답", "복습", "오답", "복습", "오답", "오답", "복습", "오답", "복습"],
  문제집: ["완료", "완료", "오답", "완료", "복습", "완료", "완료", "오답", "완료", "완료"],
  "일일면접 참여 이력": ["완료", "완료", "완료", "오답", "완료", "복습", "완료", "완료", "오답", "완료"],
};

/** 회원 상세 탭 목록 */
export const ADMIN_MEMBER_TABS = ["풀이 이력", "오답노트", "문제집", "일일면접 참여 이력"] as const;

export type AdminMemberTab = (typeof ADMIN_MEMBER_TABS)[number];

/** 회원 상세 탭별 풀이 이력 더미 (회원마다 다른 문제가 섞이도록 인덱스를 섞는다) */
export function adminSolveRecords(member: AdminMember, tab: AdminMemberTab): AdminSolveRecord[] {
  const seedIndex = adminMembers.indexOf(member);
  const pool = adminQuestions.filter((_, i) => tab === "풀이 이력" || (i + seedIndex) % 2 === 0);
  const results = SOLVE_RESULTS[tab];

  return pool.slice(0, 10).map((q, i) => {
    const result = results[i];
    return {
      at: SOLVE_DATES[i],
      questionId: q.id,
      category: q.category,
      difficulty: q.difficulty,
      title: q.title,
      type: q.type,
      score: result === "오답" ? 0 : result === "복습" ? 54 + i : 72 + ((i * 7) % 28),
      spent: SOLVE_SPENTS[i],
      result,
    };
  });
}

/** 탭별 전체 건수 (목록은 10건만 보여주므로 총계는 별도로 계산한다) */
export function adminSolveTotal(member: AdminMember, tab: AdminMemberTab): number {
  const ratio = { "풀이 이력": 1, 오답노트: 0.24, 문제집: 0.11, "일일면접 참여 이력": 0.18 }[tab];
  return Math.round(member.solvedCount * ratio);
}
