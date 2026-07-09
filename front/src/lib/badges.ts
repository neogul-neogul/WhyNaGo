// 난이도 · 등급 관련 색상/라벨 헬퍼 (디자인 시안 기준)

/** 난이도(하/중/상) 글자 색 */
export function diffColor(d: string): string {
  return { 하: "#16A34A", 중: "#D97706", 상: "#DC2626" }[d] ?? "#6B6B62";
}

/** 난이도(하/중/상) 배경 색 */
export function diffBg(d: string): string {
  return { 하: "#E8F5EE", 중: "#FEF7E8", 상: "#FEF2F2" }[d] ?? "#F1F1ED";
}

/** 등급(A/B/C/D) 색 */
export function gradeColor(g: string): string {
  return (
    { A: "#16A34A", B: "#4F46E5", C: "#D97706", D: "#DC2626" }[g] ?? "#6B6B62"
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
