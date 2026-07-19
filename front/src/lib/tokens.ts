/**
 * 디자인 토큰 팔레트 (docs/DESIGN.md §2 참고)
 *
 * globals.css의 @theme 토큰과 항상 같은 값으로 동기화한다.
 * 클래스로 표현할 수 없는 곳(동적 inline style, SVG 속성, mock 데이터)에서만 사용하고,
 * 정적 스타일은 Tailwind 유틸리티(text-ink, bg-subtle 등)를 쓴다.
 */
export const palette = {
  // 잉크 & 텍스트 위계
  ink: "#1C1C1A",
  inkHover: "#333333",
  body: "#3A3A34",
  dim: "#5A5A52",
  secondary: "#6B6B62",
  muted: "#8A8A80",
  soft: "#9A9A90",
  placeholder: "#A8A8A0",
  axis: "#B0B0A6",
  icon: "#C8C8C0",

  // 뉴트럴 배경
  page: "#F6F6F4",
  neutral: "#F1F1ED",
  subtle: "#FAFAF7",
  paper: "#FCFCFA",

  // 뉴트럴 보더
  lineStrong: "#DCDCD4",
  lineInput: "#E0E0DA",
  line: "#E6E6E0",
  lineCard: "#ECECE8",
  lineSoft: "#F0F0EC",

  // 액센트 (인디고 — 객관식/선택)
  accent: "#4F46E5",
  accentBg: "#EEF0FF",
  accentFaint: "#F5F6FF",
  accentLine: "#C7CCFF",

  // AI (바이올렛 — AI/서술형)
  ai: "#6D28D9",
  aiDeep: "#5B21B6",
  aiBg: "#F0EDFF",
  aiLine: "#E0D8FF",

  // 정답/성공
  success: "#16A34A",
  successBg: "#F2FBF5",
  successPale: "#E8F5EE",
  successBright: "#5DDC8A",
  successGlow: "#34D36B",

  // 오답/위험
  danger: "#DC2626",
  dangerBg: "#FEF2F2",

  // 난이도 중 (앰버)
  warning: "#D97706",
  warningBg: "#FEF7E8",

  // 오답 분석/경고 (오렌지)
  alert: "#C2410C",
  alertBg: "#FEF4F2",
  alertTint: "#FEF2E8",
  alertLine: "#F6DAD3",
  alertDeep: "#7A342A",

  // 스트릭
  streak: "#EA580C",
} as const;

export type PaletteColor = keyof typeof palette;
