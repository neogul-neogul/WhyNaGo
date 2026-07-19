"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import type { ProfileMenuIcon } from "@/types";
import type { Position } from "@/types";
import { navItems, profileMenu } from "@/mocks/navigation";
import { learningStats } from "@/mocks/user";
import { logout as authLogout, useAuth, useCurrentUser, useHydrated } from "@/lib/auth";

// 직무(Position)를 화면 표기용 한글 라벨로 변환
const POSITION_LABEL: Record<Position, string> = {
  BACKEND: "백엔드",
  FRONTEND: "프론트엔드",
  FULLSTACK: "풀스택",
};

// 프로필 메뉴 아이콘 (더미 UI용 인라인 SVG)
function MenuIcon({ name }: { name: ProfileMenuIcon }) {
  const common = {
    width: 16,
    height: 16,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.9,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };
  switch (name) {
    case "records":
      return (
        <svg {...common}>
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
          <path d="M14 2v6h6M8 13h8M8 17h6" />
        </svg>
      );
    case "progress":
      return (
        <svg {...common}>
          <path d="M3 3v18h18" />
          <path d="M7 15l4-4 3 3 5-6" />
        </svg>
      );
    case "weekly":
      return (
        <svg {...common}>
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
          <path d="M14 2v6h6M8 13h8M8 17h5" />
        </svg>
      );
    case "user":
      return (
        <svg {...common}>
          <circle cx="12" cy="8" r="4" />
          <path d="M4 21v-1a6 6 0 016-6h4a6 6 0 016 6v1" />
        </svg>
      );
    case "settings":
      return (
        <svg {...common}>
          <path d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.7 21a2 2 0 01-3.4 0" />
        </svg>
      );
    case "logout":
      return (
        <svg {...common}>
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
          <path d="M16 17l5-5-5-5M21 12H9" />
        </svg>
      );
  }
}

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const loggedIn = useAuth();
  const hydrated = useHydrated();
  const user = useCurrentUser();
  const [profileOpen, setProfileOpen] = useState(false);

  const isActive = (href: string) =>
    href === "/" ? pathname === "/" : pathname.startsWith(href);

  // 인증 화면(로그인/회원가입)에서는 공통 헤더를 숨긴다
  if (pathname === "/login" || pathname === "/signup") return null;

  // 클라이언트 측 더미 로그아웃: 세션 정보를 비우고 메인 페이지로 이동
  const handleLogout = () => {
    setProfileOpen(false);
    if (!window.confirm("로그아웃 하시겠어요?")) return;
    authLogout();
    router.push("/");
  };

  return (
    <header className="sticky top-0 z-30 border-b border-line bg-neutral py-[11px]">
      <div className="mx-auto flex w-full max-w-[1180px] items-center gap-6 px-9">
        {/* 로고 */}
        <Link href="/" className="flex flex-shrink-0 items-center gap-2.5">
          <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-[9px] bg-ink">
            <span className="font-mono text-[15px] font-bold tracking-[-0.5px] text-white">
              &lt;/&gt;
            </span>
          </div>
          <div className="flex flex-col leading-[1.15]">
            <span className="text-[15px] font-bold tracking-[-0.3px] text-ink">
              WhyNaGo
            </span>
            <span className="text-[11px] font-medium text-soft">
              개발자 학습 · 면접
            </span>
          </div>
        </Link>

        {/* 내비게이션 */}
        <nav className="flex min-w-0 flex-1 items-center justify-center gap-2.5 overflow-x-auto px-7">
          {navItems.map((item) => {
            const active = isActive(item.href);
            return (
              <Link
                key={item.key}
                href={item.href}
                className={`flex items-center gap-2 whitespace-nowrap rounded-[9px] px-[13px] py-2 text-sm transition-all ${
                  active
                    ? "bg-white font-semibold text-ink shadow-[0_1px_2px_rgba(0,0,0,0.06)]"
                    : "font-medium text-secondary hover:bg-black/[0.04]"
                }`}
              >
                <span>{item.label}</span>
                {item.badge && (
                  <span className="rounded-[5px] bg-ai-bg px-1.5 py-0.5 text-[10px] font-bold tracking-[0.03em] text-ai">
                    {item.badge}
                  </span>
                )}
              </Link>
            );
          })}
        </nav>

        {/* 우측: 로그인 시 스트릭 + 프로필, 로그아웃 시 로그인 버튼 */}
        <div className="relative flex flex-shrink-0 items-center gap-[11px]">
          {!hydrated ? null : loggedIn && user ? (
            <>
          <div className="flex items-center gap-1 font-mono text-xs font-semibold text-streak">
            🔥{learningStats.streakDays}
          </div>

          <button
            type="button"
            onClick={() => setProfileOpen((v) => !v)}
            className="flex items-center gap-[11px] rounded-[10px] px-2 py-[5px] transition-colors hover:bg-black/[0.04]"
          >
            <div className="flex h-[34px] w-[34px] flex-shrink-0 items-center justify-center rounded-full bg-accent text-sm font-semibold text-white">
              {user.nickname.charAt(0)}
            </div>
            <div className="flex flex-col text-left leading-[1.25]">
              <span className="text-[13px] font-semibold text-ink">
                {user.nickname}
              </span>
              <span className="text-[11px] text-soft">{POSITION_LABEL[user.position]}</span>
            </div>
            <svg
              width="15"
              height="15"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className={`text-soft transition-transform ${profileOpen ? "rotate-180" : ""}`}
            >
              <path d="M6 9l6 6 6-6" />
            </svg>
          </button>

          {/* 드롭다운 */}
          {profileOpen && (
            <>
              {/* 바깥 클릭 시 닫기 */}
              <button
                type="button"
                aria-label="메뉴 닫기"
                className="fixed inset-0 z-40 cursor-default"
                onClick={() => setProfileOpen(false)}
              />
              <div className="absolute right-0 top-[calc(100%+8px)] z-50 flex w-[236px] flex-col gap-0.5 rounded-[14px] border border-line-card bg-white p-2 shadow-[0_12px_32px_rgba(0,0,0,0.12)]">
                <div className="mb-1 flex items-center gap-[11px] border-b border-line-soft px-3 pb-3 pt-2.5">
                  <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-accent text-base font-semibold text-white">
                    {user.nickname.charAt(0)}
                  </div>
                  <div className="flex min-w-0 flex-col leading-[1.3]">
                    <span className="text-sm font-bold text-ink">
                      {user.nickname}
                    </span>
                    <span className="truncate text-xs text-soft">
                      {user.email}
                    </span>
                  </div>
                </div>
                {profileMenu.map((m) => {
                  const itemClass = `flex w-full items-center gap-2.5 rounded-[9px] px-3 py-2.5 text-left text-[13px] font-medium transition-colors hover:bg-subtle ${
                    m.danger ? "text-danger" : "text-body"
                  }`;

                  if (m.action === "logout") {
                    return (
                      <button
                        key={m.label}
                        type="button"
                        onClick={handleLogout}
                        className={itemClass}
                      >
                        <MenuIcon name={m.icon} />
                        <span>{m.label}</span>
                      </button>
                    );
                  }

                  return (
                    <Link
                      key={m.label}
                      href={m.href}
                      onClick={() => setProfileOpen(false)}
                      className={itemClass}
                    >
                      <MenuIcon name={m.icon} />
                      <span>{m.label}</span>
                    </Link>
                  );
                })}
              </div>
            </>
          )}
            </>
          ) : (
            <Link
              href="/login"
              className="rounded-[10px] bg-ink px-[18px] py-2 text-[13px] font-semibold text-white transition-colors hover:bg-ink-hover"
            >
              로그인
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
