"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { isComingSoon } from "@/lib/comingSoon";

export default function ComingSoonGate({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();

  if (!isComingSoon(pathname)) return <>{children}</>;

  return (
    <div className="flex min-w-0 flex-1 flex-col">
      <div
        aria-hidden
        className="pointer-events-none flex min-w-0 flex-1 select-none flex-col opacity-50 blur-[3.5px]"
      >
        {children}
      </div>

      <div className="fixed inset-0 z-20 flex items-center justify-center bg-page/45 px-5">
        <div className="flex w-full max-w-110 flex-col items-center rounded-[18px] border border-line-card bg-white px-10 py-11 text-center">
          <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-ai-bg text-ai-deep">
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
              <path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z" />
            </svg>
          </div>

          <h2 className="mb-6 text-lg font-semibold text-ink">
            준비 중인 기능이에요
          </h2>

          <Link
            href="/"
            className="rounded-[10px] bg-ink px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-ink-hover"
          >
            홈으로
          </Link>
        </div>
      </div>
    </div>
  );
}