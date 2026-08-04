import { apiFetch } from "@/lib/api";
import type { DailyRecordCountResponse, GrassDay, RecentRecordResponse, StreakResponse } from "@/types";

// 학습 기록 도메인 API — 잔디 등급(0~4단계)·"학습량 점수"는 백엔드에 없는 개념이라(docs/DOMAIN.md 보류)
// 일자별 문항 수(questionCount)를 바탕으로 프런트에서 계산한다.

export const grassColors = ["#EBEDF0", "#9BE9A8", "#40C463", "#30A14E", "#216E39"];

export const levelGuide = [
  { color: grassColors[0], label: "학습 없음" },
  { color: grassColors[1], label: "문제 1개 이상" },
  { color: grassColors[2], label: "문제 5개 이상" },
  { color: grassColors[3], label: "문제 10개 이상" },
  { color: grassColors[4], label: "문제 20개 이상" },
];

/** 일자별 문항 수 → 잔디 등급(0~4). 등급 구간은 확정된 정책이 없어 프런트에서 임시로 정한 값이다 */
function levelOf(questionCount: number): number {
  if (questionCount <= 0) return 0;
  if (questionCount < 5) return 1;
  if (questionCount < 10) return 2;
  if (questionCount < 20) return 3;
  return 4;
}

const WEEKS = 53;
const DAYS_PER_WEEK = 7;
const GRASS_RANGE_DAYS = WEEKS * DAYS_PER_WEEK - 1;

/** Date → "yyyy-MM-dd" (로컬 기준) */
function toDateKey(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** 오늘 날짜의 "yyyy-MM-dd" (로컬 기준) */
export function todayDateKey(): string {
  return toDateKey(new Date());
}

/** 잔디 그리드와 동일한 범위(오늘로부터 53주 전 ~ 오늘)로 daily-counts를 조회하기 위한 from/to */
export function defaultGrassRange(): { from: string; to: string } {
  const to = new Date();
  const from = new Date(to);
  from.setDate(from.getDate() - GRASS_RANGE_DAYS);
  return { from: toDateKey(from), to: toDateKey(to) };
}

/**
 * 최근 53주(371일) 달력 그리드를 만들고, 학습이 있었던 날짜만 담긴 daily-counts를
 * 해당 칸에 채워 넣는다. 학습이 없었던 날짜는 0으로 간주한다(→ docs/API.md).
 */
export function toGrassWeeks(dailyCounts: DailyRecordCountResponse[]): { days: GrassDay[] }[] {
  const countByDate = new Map(dailyCounts.map((d) => [d.date, d]));
  const today = new Date();

  const allDays: GrassDay[] = [];
  for (let i = GRASS_RANGE_DAYS; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const key = toDateKey(date);
    const questionCount = countByDate.get(key)?.questionCount ?? 0;
    const level = levelOf(questionCount);
    allDays.push({ date: key, level, color: grassColors[level], count: questionCount });
  }

  const weeks: { days: GrassDay[] }[] = [];
  for (let w = 0; w < WEEKS; w++) {
    weeks.push({ days: allDays.slice(w * DAYS_PER_WEEK, (w + 1) * DAYS_PER_WEEK) });
  }
  return weeks;
}

/** 최근 학습 기록 목록 조회. size를 생략하면 서버 기본값(20)이 적용된다 */
export function fetchRecentRecords(size?: number): Promise<RecentRecordResponse[]> {
  const query = size === undefined ? "" : `?size=${size}`;
  return apiFetch<RecentRecordResponse[]>(`/api/learning-records/recent${query}`);
}

/** 연속·누적 학습일 조회 */
export function fetchStreak(): Promise<StreakResponse> {
  return apiFetch<StreakResponse>("/api/learning-records/streak");
}

/** 일자별 학습량(잔디) 조회. from/to를 생략하면 서버 기본 범위(오늘로부터 364일 전)가 적용된다 */
export function fetchDailyCounts(from?: string, to?: string): Promise<DailyRecordCountResponse[]> {
  const params = new URLSearchParams();
  if (from) params.set("from", from);
  if (to) params.set("to", to);
  const query = params.toString();
  return apiFetch<DailyRecordCountResponse[]>(`/api/learning-records/daily-counts${query ? `?${query}` : ""}`);
}

/** ISO LocalDateTime("2026-06-25T10:00:00") → "06.25" */
export function formatRecordDate(solvedAt: string): string {
  return solvedAt.slice(5, 10).replaceAll("-", ".");
}

/** startedAt~solvedAt 소요시간을 "N분"으로 표시 */
export function formatDuration(startedAt: string | null, solvedAt: string): string {
  if (startedAt === null) return "기록 없음";

  const ms = new Date(solvedAt).getTime() - new Date(startedAt).getTime();
  if (Number.isNaN(ms) || ms < 0) return "기록 없음";

  const minutes = Math.round(ms / 60000);
  return minutes === 0 ? "1분 미만" : `${minutes}분`;
}