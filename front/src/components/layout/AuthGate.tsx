"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { subscribeSessionExpired } from "@/lib/api";
import { useAuth, useHydrated } from "@/lib/auth";
import { isGuestOnlyRoute, isPublicRoute } from "@/lib/routes";
import LoginRequiredGate from "@/components/layout/LoginRequiredGate";

/**
 * 인증 상태에 따라 페이지 접근을 판정한다.
 * - 비로그인 + 보호 경로: 페이지 위에 안내 모달을 띄우고 이동하지 않는다. 모달 배경이
 *   조작을 막으므로 페이지는 보이기만 한다.
 * - 로그인 상태 + 로그인/회원가입: 홈으로 보낸다.
 * - 사용 중 세션 만료: 로그인 화면으로 보내고 만료 사유를 전달한다.
 */
export default function AuthGate({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const loggedIn = useAuth();
  const hydrated = useHydrated();

  useEffect(
    () =>
      subscribeSessionExpired(() => {
        const redirect = encodeURIComponent(pathname);
        router.replace(`/login?redirect=${redirect}&reason=expired`);
      }),
    [pathname, router],
  );

  useEffect(() => {
    if (!hydrated || !loggedIn) return;
    if (isGuestOnlyRoute(pathname)) router.replace("/");
  }, [hydrated, loggedIn, pathname, router]);

  if (isPublicRoute(pathname)) return <>{children}</>;
  if (!hydrated) return null;
  return (
    <>
      {children}
      {!loggedIn && <LoginRequiredGate />}
    </>
  );
}