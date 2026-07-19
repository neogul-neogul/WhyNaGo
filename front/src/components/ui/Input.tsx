import type { InputHTMLAttributes } from "react";

// 공통 텍스트 인풋 (docs/DESIGN.md §7) — 여백(mb-*) 등 비충돌 유틸리티만 className으로 추가한다
export default function Input({
  className = "",
  ...props
}: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={`w-full rounded-[11px] border border-line-input bg-subtle px-[15px] py-[13px] text-sm text-ink outline-none placeholder:text-placeholder focus:border-ink ${className}`}
      {...props}
    />
  );
}
