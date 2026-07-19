import { palette } from "@/lib/tokens";
import type { BadgeTone } from "@/components/ui/Badge";

// 난이도 · 등급 관련 색상/라벨 헬퍼 (docs/DESIGN.md §2.4 상태 페어)

/** 난이도(하/중/상) Badge 톤 */
export function diffTone(d: string): BadgeTone {
  return ({ 하: "success", 중: "warning", 상: "danger" } as const)[d] ?? "neutral";
}

/** 난이도(하/중/상) 글자 색 — inline style/SVG 등 클래스 밖 용도 */
export function diffColor(d: string): string {
  return (
    { 하: palette.success, 중: palette.warning, 상: palette.danger }[d] ??
    palette.secondary
  );
}

/** 난이도(하/중/상) 배경 색 — inline style 등 클래스 밖 용도 */
export function diffBg(d: string): string {
  return (
    { 하: palette.successPale, 중: palette.warningBg, 상: palette.dangerBg }[d] ??
    palette.neutral
  );
}

/** 등급(A/B/C/D) 색 */
export function gradeColor(g: string): string {
  return (
    { A: palette.success, B: palette.accent, C: palette.warning, D: palette.danger }[g] ??
    palette.secondary
  );
}

/** 난이도 기본값 보정 */
export function lvBadge(d?: string): string {
  return d || "중";
}

/** 문제 카테고리 목록 (필터 등에서 공용 사용) */
export const CATEGORIES = [
  "전체",
  "DB",
  "네트워크",
  "알고리즘",
  "자료구조",
  "운영체제",
  "디자인패턴",
  "언어",
] as const;
