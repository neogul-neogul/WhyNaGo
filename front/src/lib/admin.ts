import { apiFetch } from "@/lib/api";
import { DIFFICULTY_LABELS, type QuestionFilters, type QuestionPaging } from "@/lib/questions";
import type {
  AdminDashboardAlert,
  AdminKpi,
  AdminMemberDetailResponse,
  AdminMemberResponse,
  AdminMemberSummaryResponse,
  AdminQuestionDetailResponse,
  AdminQuestionResponse,
  DashboardAlertCode,
  DashboardAlertResponse,
  DashboardResponse,
  MetricComparisonResponse,
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
  "GENERAL_CS",
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

// ===== 대시보드 =====

/** 관리자 대시보드 조회 — KPI와 운영 알림을 한 번에 받는다 */
export function fetchAdminDashboard(): Promise<DashboardResponse> {
  return apiFetch<DashboardResponse>("/api/admin/dashboard");
}

const KPI_LABELS = {
  totalMember: "전체 회원 수",
  activeMember: "최근 7일 활동 회원 수",
  cumulativeSolve: "누적 풀이 수",
  interview: "오늘 면접 참여 / 완료",
  signUp: "오늘 가입자 수",
  solve: "오늘 풀이 수",
} as const;

/** 조회 전에도 카드 레이아웃을 유지하기 위한 자리 표시 */
export const DASHBOARD_KPI_PLACEHOLDERS: AdminKpi[] = Object.values(KPI_LABELS).map((label) => ({
  label,
  value: "—",
}));

/**
 * 전일·전주 대비 증감 표기.
 * 서버는 { current, previous } 두 숫자만 주므로 화살표·퍼센트 문자열은 여기서 만든다.
 * previousText를 주면 괄호 안 값을 대체한다 (예: 면접 카드의 "435 / 340").
 */
function formatDelta(
  comparison: MetricComparisonResponse,
  previousLabel: "전일" | "전주",
  previousText?: string,
): Pick<AdminKpi, "delta" | "increased"> {
  const { current, previous } = comparison;
  const previousPart = `(${previousLabel} ${previousText ?? previous.toLocaleString()})`;

  // 이전 값이 0이면 증감률을 낼 수 없으므로 비교 대상만 보여준다
  if (previous === 0) {
    return { delta: previousPart, increased: current > 0 };
  }

  const rate = ((current - previous) / previous) * 100;
  const arrow = rate > 0 ? "▲ " : rate < 0 ? "▼ " : "";
  return { delta: `${arrow}${Math.abs(rate).toFixed(1)}% ${previousPart}`, increased: rate >= 0 };
}

/** 대시보드 응답 → KPI 카드 6장 */
export function toDashboardKpis(dashboard: DashboardResponse): AdminKpi[] {
  const { started, completed } = dashboard.todayInterview;
  const { total, multipleChoiceCount, essayCount } = dashboard.cumulativeSolveCount;

  return [
    {
      label: KPI_LABELS.totalMember,
      value: dashboard.totalMemberCount.toLocaleString(),
      unit: "명",
    },
    {
      label: KPI_LABELS.activeMember,
      value: dashboard.activeMember7Days.current.toLocaleString(),
      unit: "명",
      ...formatDelta(dashboard.activeMember7Days, "전주"),
    },
    {
      label: KPI_LABELS.cumulativeSolve,
      value: total.toLocaleString(),
      unit: "건",
      breakdown: `객관식 ${multipleChoiceCount.toLocaleString()} · 서술형 ${essayCount.toLocaleString()}`,
    },
    {
      label: KPI_LABELS.interview,
      value: `${started.current.toLocaleString()} / ${completed.current.toLocaleString()}`,
      ...formatDelta(
        started,
        "전일",
        `${started.previous.toLocaleString()} / ${completed.previous.toLocaleString()}`,
      ),
    },
    {
      label: KPI_LABELS.signUp,
      value: dashboard.todaySignUpCount.current.toLocaleString(),
      unit: "명",
      ...formatDelta(dashboard.todaySignUpCount, "전일"),
    },
    {
      label: KPI_LABELS.solve,
      value: dashboard.todaySolveCount.current.toLocaleString(),
      unit: "건",
      ...formatDelta(dashboard.todaySolveCount, "전일"),
    },
  ];
}

/**
 * 알림 종류별 표시 문구와 CTA. 서버는 코드와 판정에 쓴 값만 주고, 라우트 지식은 프런트가 갖는다.
 * 미고정은 첫 응시자가 면접을 시작하면 해소되는 상태라 경고가 아닌 안내 문구로 쓴다.
 */
const DASHBOARD_ALERT_PRESETS: Record<DashboardAlertCode, AdminDashboardAlert> = {
  DAILY_INTERVIEW_NOT_PINNED: {
    title: "1일1면접 미고정",
    detail: "오늘 면접 문항이 아직 고정되지 않았습니다 (첫 응시자가 면접을 시작하면 고정됩니다)",
    ctaLabel: "1일1면접 이력으로 이동",
    ctaHref: "/admin/interviews",
  },
};

/** 대시보드 응답의 알림 → 알림 카드. 서버가 새 종류를 추가해도 화면이 깨지지 않게 모르는 코드는 건너뛴다 */
export function toDashboardAlerts(alerts: DashboardAlertResponse[]): AdminDashboardAlert[] {
  return alerts.flatMap((alert) => {
    const preset = DASHBOARD_ALERT_PRESETS[alert.type];
    return preset ? [preset] : [];
  });
}

// ===== 회원 관리 =====

/** 관리자 회원 목록 한 페이지의 행 수 */
export const ADMIN_MEMBER_PAGE_SIZE = 8;

/** 관리자 회원 목록 조회 — 정렬은 가입 역순 고정이고 티어 정렬·필터는 서버가 지원하지 않는다 */
export function fetchAdminMembers(
  keyword?: string,
  paging: QuestionPaging = {},
): Promise<PageResponse<AdminMemberResponse>> {
  const params = new URLSearchParams();
  if (keyword) params.set("q", keyword);
  params.set("page", String(paging.page ?? 0));
  params.set("size", String(paging.size ?? ADMIN_MEMBER_PAGE_SIZE));
  return apiFetch<PageResponse<AdminMemberResponse>>(`/api/admin/members?${params.toString()}`);
}

/** 목록 상단 요약 — PageResponse에 끼울 수 없어 서버도 엔드포인트를 나눠 뒀다 */
export function fetchAdminMemberSummary(): Promise<AdminMemberSummaryResponse> {
  return apiFetch<AdminMemberSummaryResponse>("/api/admin/members/summary");
}

/** 회원 상세 — 스트릭·풀이 문항 수·완료 면접 수는 모달을 열 때만 조회한다 */
export function fetchAdminMemberDetail(userId: number): Promise<AdminMemberDetailResponse> {
  return apiFetch<AdminMemberDetailResponse>(`/api/admin/members/${userId}`);
}

/** 가입 시각("2025-11-02T09:12:00") → 날짜만("2025-11-02"). 추적 이전 가입 회원은 "-" */
export function formatJoinedDate(createdAt: string | null): string {
  if (!createdAt) return "-";
  return createdAt.slice(0, 10);
}
