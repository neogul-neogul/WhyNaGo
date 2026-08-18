"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { logout, useCurrentUser } from "@/lib/auth";
import Button from "@/components/ui/Button";
import AdminLogo from "@/components/admin/AdminLogo";

const NAV_ITEMS = [
  { href: "/admin", label: "대시보드" },
  { href: "/admin/members", label: "회원관리" },
  { href: "/admin/questions", label: "문제관리" },
  { href: "/admin/interviews", label: "1일1면접 이력" },
  { href: "/admin/emails", label: "이메일 발송 관리" },
] as const;

// 상단바 제목은 경로에서 끌어낸다 (문제 상세만 문제 ID를 덧붙인다)
function pageTitle(pathname: string): string {
  const [, , section, id, sub] = pathname.split("/");
  if (section === undefined) return "대시보드";
  if (section === "members") return id ? "회원 상세" : "회원 목록";
  if (section === "questions") {
    if (!id) return "문제 목록";
    if (id === "new") return "문제 등록";
    return sub === "edit" ? "문제 수정" : `문제 상세 · ${id}`;
  }
  if (section === "interviews") return "일일면접 이력";
  if (section === "emails") return "이메일 발송 관리";
  return "대시보드";
}

function isActive(pathname: string, href: string) {
  return href === "/admin" ? pathname === "/admin" : pathname.startsWith(href);
}

export default function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const user = useCurrentUser();

  return (
    <div className="flex min-h-screen w-full bg-page">
      <aside className="sticky top-0 flex h-screen w-[300px] flex-shrink-0 flex-col gap-[26px] border-r border-line px-4 py-[22px]">
        <div className="flex h-10 items-center gap-2.5 px-2">
          <AdminLogo />
          <span className="text-xl font-extrabold tracking-[-0.4px] text-ink">WhyNaGo</span>
          <span className="text-sm font-semibold text-placeholder">Admin</span>
        </div>

        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`rounded-[11px] px-4 py-3.5 text-[17px] transition-colors ${
                isActive(pathname, item.href)
                  ? "bg-ink font-bold text-white"
                  : "font-medium text-secondary hover:bg-neutral"
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 flex items-center justify-between gap-6 border-b border-line bg-page/90 px-8 py-[18px] backdrop-blur">
          <h1 className="text-[22px] font-bold tracking-[-0.4px] text-ink">{pageTitle(pathname)}</h1>
          <div className="flex flex-shrink-0 items-center gap-4">
            <span className="whitespace-nowrap text-[13.5px] font-medium text-secondary">
              {user?.nickname ?? "관리자"} · 관리자
            </span>
            <Button
              variant="secondary"
              size="md"
              onClick={() => {
                void logout();
                router.replace("/admin");
              }}
            >
              로그아웃
            </Button>
          </div>
        </header>

        <main className="min-w-0 flex-1 px-8 pb-[60px] pt-7">{children}</main>
      </div>
    </div>
  );
}
