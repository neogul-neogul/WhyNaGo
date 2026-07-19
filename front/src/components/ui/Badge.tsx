export type BadgeTone =
  | "accent"
  | "ai"
  | "success"
  | "danger"
  | "warning"
  | "alert"
  | "neutral"
  | "ink";

export type BadgeSize = "xs" | "sm";

const TONE_CLASS: Record<BadgeTone, string> = {
  accent: "bg-accent-bg text-accent",
  ai: "bg-ai-bg text-ai",
  success: "bg-success-bg text-success",
  danger: "bg-danger-bg text-danger",
  warning: "bg-warning-bg text-warning",
  alert: "bg-alert-tint text-alert",
  neutral: "bg-neutral text-secondary",
  ink: "bg-ink text-white",
};

const SIZE_CLASS: Record<BadgeSize, string> = {
  xs: "rounded-[5px] px-[9px] py-0.5 text-[11px] font-bold",
  sm: "rounded-[6px] px-2.5 py-[3px] text-xs font-semibold",
};

// 상태/유형 배지 (docs/DESIGN.md §6.3 — 전경/배경 페어)
export default function Badge({
  tone = "neutral",
  size = "sm",
  className = "",
  children,
}: {
  tone?: BadgeTone;
  size?: BadgeSize;
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <span
      className={`inline-flex w-fit items-center whitespace-nowrap ${TONE_CLASS[tone]} ${SIZE_CLASS[size]} ${className}`}
    >
      {children}
    </span>
  );
}
