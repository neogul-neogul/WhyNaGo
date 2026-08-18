"use client";

import { useHydrated, useIsAdmin } from "@/lib/auth";
import AdminLogin from "@/components/admin/AdminLogin";
import AdminShell from "@/components/admin/AdminShell";

/**
 * 관리자 화면 접근 판정.
 * 로그인 세션은 사용자 화면과 공유하되 role이 ADMIN일 때만 통과시킨다
 * (AuthGate는 /admin을 그냥 통과시키고, 여기서만 판정한다).
 */
export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const hydrated = useHydrated();
  const admin = useIsAdmin();

  if (!hydrated) return null;
  if (!admin) return <AdminLogin />;
  return <AdminShell>{children}</AdminShell>;
}
