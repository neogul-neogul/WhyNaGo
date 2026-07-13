"use client";

// 필터/선택용 공통 칩 버튼 (디자인 시안 기준)
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
      className="cursor-pointer rounded-[9px] px-[15px] py-2 text-[13.5px] transition-all"
      style={{
        fontWeight: active ? 600 : 500,
        border: `1px solid ${active ? "#1C1C1A" : "#E0E0DA"}`,
        background: active ? "#1C1C1A" : "#fff",
        color: active ? "#fff" : "#5A5A52",
      }}
    >
      {label}
    </button>
  );
}