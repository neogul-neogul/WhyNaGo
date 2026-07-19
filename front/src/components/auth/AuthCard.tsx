"use client";

import type { FormEventHandler } from "react";

// 인증(로그인/회원가입) 카드 골격: 로고 + 브랜드 + 부제 + 폼 내용
export default function AuthCard({
  subtitle,
  onSubmit,
  children,
}: {
  subtitle: string;
  onSubmit: FormEventHandler<HTMLFormElement>;
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-1 items-center justify-center p-6">
      <form
        onSubmit={onSubmit}
        className="flex w-full max-w-[400px] flex-col items-center gap-1.5 rounded-[20px] border border-line-card bg-white px-9 py-10 shadow-[0_20px_50px_rgba(0,0,0,0.08)]"
      >
        {/* 로고 */}
        <div className="mb-2 flex h-[52px] w-[52px] items-center justify-center rounded-[14px] bg-ink">
          <span className="font-mono text-[23px] font-bold tracking-[-0.5px] text-white">
            &lt;/&gt;
          </span>
        </div>
        <span className="text-xl font-bold tracking-[-0.4px] text-ink">
          WhyNaGo
        </span>
        <span className="mb-6 text-[13.5px] text-soft">{subtitle}</span>
        {children}
      </form>
    </div>
  );
}
