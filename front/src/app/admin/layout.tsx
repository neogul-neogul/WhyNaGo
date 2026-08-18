"use client";

import { useAdminAuth } from "@/lib/adminAuth";
import { useHydrated } from "@/lib/auth";
import AdminLogin from "@/components/admin/AdminLogin";
import AdminShell from "@/components/admin/AdminShell";

/**
 * 관리자 화면 접근 판정.
 * 사용자 세션과 무관하게 관리자 로그인 여부만 본다 (AuthGate는 /admin을 그냥 통과시킨다).
 */
export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const hydrated = useHydrated();
  const loggedIn = useAdminAuth();

  if (!hydrated) return null;
  if (!loggedIn) return <AdminLogin />;
  return <AdminShell>{children}</AdminShell>;
}
