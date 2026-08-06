/**
 * 로그인 없이 접근할 수 있는 경로 화이트리스트.
 * 여기에 없는 경로는 전부 보호 대상이므로, 새 페이지를 추가하면 기본값이 "로그인 필요"다.
 */
const PUBLIC_ROUTES = ["/", "/solve", "/login", "/signup"] as const;

/** 로그인한 사용자에게는 의미가 없어 홈으로 돌려보내는 경로 */
const GUEST_ONLY_ROUTES = ["/login", "/signup"] as const;

/** 비로그인으로 볼 수 있는 경로인지 (하위 경로는 포함하지 않는다 — /solve는 공개지만 /solve/1은 아니다) */
export function isPublicRoute(pathname: string): boolean {
  return PUBLIC_ROUTES.some((route) => route === pathname);
}

/** 로그인 상태로 접근하면 홈으로 보내야 하는 경로인지 */
export function isGuestOnlyRoute(pathname: string): boolean {
  return GUEST_ONLY_ROUTES.some((route) => route === pathname);
}