"use client";

import { useEffect, useRef, useState } from "react";

/**
 * 관리자 화면 공통 드롭다운 필터.
 * 회원 티어 · 문제 카테고리/난이도/유형 · 면접 카테고리에서 함께 쓴다.
 */
export default function FilterSelect({
  label,
  value,
  options,
  onChange,
  variant = "box",
}: {
  /** 지정하면 버튼에 "라벨 : 값" 형태로 표시한다 */
  label?: string;
  value: string;
  options: string[];
  onChange: (value: string) => void;
  /** box: 목록 상단 필터 바, chip: 칩 열에 섞여 들어가는 둥근 형태 */
  variant?: "box" | "chip";
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const close = (e: MouseEvent) => {
      if (!ref.current?.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  const isDefault = value === "전체" || value.endsWith("전체");

  return (
    <div ref={ref} className="relative flex-shrink-0">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className={`flex cursor-pointer items-center gap-2.5 whitespace-nowrap border border-line-input bg-white font-semibold transition-colors hover:border-ink ${
          variant === "box"
            ? "rounded-[12px] px-[18px] py-[14px] text-sm"
            : "rounded-[20px] px-4 py-[9px] text-[13px]"
        } ${isDefault ? "text-secondary" : "text-ink"}`}
      >
        {label ? `${label} : ${value}` : value}
        <span className="text-[11px] text-soft">▾</span>
      </button>

      {open && (
        <div className="absolute left-0 top-[calc(100%+6px)] z-20 flex max-h-[280px] min-w-[180px] flex-col overflow-auto rounded-[12px] border border-line bg-white p-1.5 shadow-[0_12px_30px_rgba(0,0,0,0.08)]">
          {options.map((option) => (
            <button
              key={option}
              type="button"
              onClick={() => {
                onChange(option);
                setOpen(false);
              }}
              className={`cursor-pointer rounded-[8px] px-3 py-2.5 text-left text-[13.5px] font-semibold text-ink transition-colors hover:bg-subtle ${
                option === value ? "bg-neutral" : "bg-white"
              }`}
            >
              {option}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
