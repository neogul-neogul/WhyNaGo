import type { ButtonHTMLAttributes } from "react";

export type ButtonVariant = "primary" | "secondary" | "muted" | "ai" | "success";
export type ButtonSize = "sm" | "md" | "lg" | "xl";

const VARIANT_CLASS: Record<ButtonVariant, string> = {
  // 주요 액션 (잉크)
  primary:
    "bg-ink text-white hover:bg-ink-hover disabled:bg-icon disabled:hover:bg-icon",
  // 보조 액션 (흰 배경 + 테두리, 잉크 라벨)
  secondary: "border border-line-strong bg-white text-ink hover:border-ink",
  // 보조 액션 (흰 배경 + 테두리, 연한 라벨) — 취소/종료 등
  muted: "border border-line-strong bg-white text-secondary hover:border-ink",
  // AI 관련 흐름 전용 (바이올렛)
  ai: "bg-ai text-white",
  // 완료성 액션 (그린)
  success: "bg-success text-white",
};

const SIZE_CLASS: Record<ButtonSize, string> = {
  sm: "rounded-[8px] px-3.5 py-1.5 text-[12.5px]",
  md: "rounded-[10px] px-[22px] py-[11px] text-[13.5px]",
  lg: "rounded-[10px] px-[26px] py-[11px] text-[14px]",
  xl: "rounded-[11px] px-7 py-[13px] text-[15px]",
};

// 공통 버튼 (docs/DESIGN.md §6.1)
export default function Button({
  variant = "primary",
  size = "lg",
  type = "button",
  className = "",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
}) {
  return (
    <button
      type={type}
      className={`cursor-pointer font-semibold transition-colors disabled:cursor-not-allowed ${VARIANT_CLASS[variant]} ${SIZE_CLASS[size]} ${className}`}
      {...props}
    />
  );
}
