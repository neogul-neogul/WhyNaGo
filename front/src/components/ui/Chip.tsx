"use client";

// 필터/선택용 공통 칩 버튼 (docs/DESIGN.md §6.2)
export default function Chip({
  label,
  active,
  onClick,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`cursor-pointer rounded-[9px] border px-[15px] py-2 text-[13.5px] transition-all ${
        active
          ? "border-ink bg-ink font-semibold text-white"
          : "border-line-input bg-white font-medium text-dim"
      }`}
    >
      {label}
    </button>
  );
}
