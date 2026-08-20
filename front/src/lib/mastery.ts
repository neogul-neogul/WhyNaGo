import { apiFetch } from "@/lib/api";
import { palette } from "@/lib/tokens";
import type { MasteryLevel, MasteryResponse } from "@/types";

// 숙련도 도메인 API — 판정 정책은 docs/RECOMMENDATION.md 숙련도 판정 정책 참고.
//
// 서술형은 채점 AI가 답변 **내용**을 보고 판정하고 그 근거(reason)를 함께 남긴다.
// 객관식은 서버가 정답 여부 x 평균 대비 소요시간으로 판정한다.

/** 카테고리별 판정 분포와 태그별 현재 숙련도·근거 조회 */
export function fetchMastery(): Promise<MasteryResponse> {
  return apiFetch<MasteryResponse>("/api/mastery");
}

/** 숙련도 enum → 화면 라벨 */
export const MASTERY_LEVEL_LABELS: Record<MasteryLevel, string> = {
  MASTERED: "완전 이해",
  SOLID: "이해",
  UNSTABLE: "불안정",
  GUESSED: "찍음",
  WEAK: "오개념",
  NOT_LEARNED: "미학습",
};

/** 그 판정이 무엇을 뜻하는지. 라벨만으로는 여섯 단계의 차이가 전달되지 않는다 */
export const MASTERY_LEVEL_DESCRIPTIONS: Record<MasteryLevel, string> = {
  MASTERED: "결론과 근거가 정확하고 인접 개념까지 스스로 정리했습니다.",
  SOLID: "결론이 정확하고 필요한 근거를 갖췄습니다.",
  UNSTABLE: "결론은 맞지만 근거가 틀렸거나 흔들립니다.",
  GUESSED: "근거 없이 핵심 용어만 나열했습니다.",
  WEAK: "설명을 시도했지만 결론이 틀렸습니다.",
  NOT_LEARNED: "개념 자체가 잡혀 있지 않습니다.",
};

/**
 * 잘 아는 쪽에서 모르는 쪽 순서. 판정 분포를 이 순서로 쌓으면
 * 막대 왼쪽이 항상 "아는 것"이라 카테고리끼리 눈으로 비교할 수 있다.
 */
export const MASTERY_LEVEL_ORDER: MasteryLevel[] = [
  "MASTERED",
  "SOLID",
  "UNSTABLE",
  "GUESSED",
  "WEAK",
  "NOT_LEARNED",
];

/**
 * 숙련도 색. 초록(아는 것) → 빨강(모르는 것) 한 축으로 두어 순서가 색으로도 읽히게 했다.
 * 라벨이 항상 함께 있으므로 색만으로 구분하지 않는다.
 */
export const MASTERY_LEVEL_COLORS: Record<MasteryLevel, string> = {
  MASTERED: "#2C817C",
  SOLID: palette.success,
  UNSTABLE: "#926F17",
  GUESSED: palette.warning,
  WEAK: "#C2603A",
  NOT_LEARNED: palette.danger,
};

/** 태그 하나라도 판정된 카테고리가 있는지. 전부 비면 화면을 띄우지 않는다 */
export function hasMastery(mastery: MasteryResponse): boolean {
  return mastery.categories.length > 0;
}
