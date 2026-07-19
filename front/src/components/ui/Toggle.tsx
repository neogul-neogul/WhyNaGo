"use client";

// 온/오프 토글 스위치 (docs/DESIGN.md §7)
export default function Toggle({
  on,
  onToggle,
  label,
}: {
  on: boolean;
  onToggle: () => void;
  /** 접근성용 라벨 (아이콘 전용 컨트롤이므로 필수) */
  label: string;
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={on}
      aria-label={label}
      onClick={onToggle}
      className={`relative h-[25px] w-11 flex-shrink-0 cursor-pointer rounded-[20px] transition-colors ${
        on ? "bg-success" : "bg-line-strong"
      }`}
    >
      <span
        className="absolute top-[3px] h-[19px] w-[19px] rounded-full bg-white shadow-[0_1px_2px_rgba(0,0,0,0.2)] transition-all"
        style={{ left: on ? "22px" : "3px" }}
      />
    </button>
  );
}
