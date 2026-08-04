"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import Modal from "@/components/ui/Modal";

export default function LoginRequiredGate() {
  const pathname = usePathname();

  return (
    <Modal labelledBy="login-required-title">
      <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-accent-bg text-accent">
        <svg
          width="26"
          height="26"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.9"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <rect x="4" y="10" width="16" height="11" rx="2" />
          <path d="M8 10V7a4 4 0 018 0v3" />
        </svg>
      </div>

      <h2
        id="login-required-title"
        className="mb-2 text-lg font-semibold text-ink"
      >
        로그인 후 이용할 수 있어요
      </h2>
      <p className="mb-6 text-[13.5px] text-soft">
        로그인하면 이 페이지를 바로 이어서 볼 수 있어요
      </p>

      <div className="flex w-full gap-2">
        <Link
          href={`/login?redirect=${encodeURIComponent(pathname)}`}
          className="flex-1 rounded-[11px] bg-ink px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-ink-hover"
        >
          로그인하기
        </Link>
        <Link
          href="/"
          className="flex-1 rounded-[11px] border border-line-strong bg-white px-5 py-2.5 text-sm font-semibold text-ink transition-colors hover:border-ink"
        >
          홈으로
        </Link>
      </div>
    </Modal>
  );
}