"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { isComingSoon } from "@/lib/comingSoon";
import Modal from "@/components/ui/Modal";

/**
 * 미구현(더미 데이터만 있는) 화면 접근을 안내하는 가드.
 * 비로그인 상태에서는 AuthGate가 이미 로그인 모달을 띄우므로, 여기서는 아무 것도 하지 않고
 * children만 통과시킨다 — 로그인한 사용자에게만 이 모달을 보여준다.
 */
export default function ComingSoonGate({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const loggedIn = useAuth();

  if (!isComingSoon(pathname) || !loggedIn) return <>{children}</>;

  return (
    <>
      {children}
      <Modal labelledBy="coming-soon-title">
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

        <h2
          id="coming-soon-title"
          className="mb-6 text-lg font-semibold text-ink"
        >
          준비 중인 기능이에요
        </h2>

        <Link
          href="/"
          className="rounded-[10px] bg-ink px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-ink-hover"
        >
          홈으로
        </Link>
      </Modal>
    </>
  );
}