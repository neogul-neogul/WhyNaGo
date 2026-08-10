/**
 * 로그인 없이 접근할 수 있는 경로 화이트리스트.
 * 여기에 없는 경로는 전부 보호 대상이므로, 새 페이지를 추가하면 기본값이 "로그인 필요"다.
 */
const PUBLIC_ROUTES = ["/", "/solve", "/interview", "/login", "/signup"] as const;

/**
 * 하위 경로까지 공개인 경로.
 * /solve/[id](문제 상세)는 조회는 공개이고, 풀이 액션(선택지 클릭·답안 입력)만
 * 각 퀴즈 컴포넌트에서 로그인 여부를 따로 확인해 막는다.
 */
const PUBLIC_PREFIX_ROUTES = ["/solve"] as const;

/** 로그인한 사용자에게는 의미가 없어 홈으로 돌려보내는 경로 */
const GUEST_ONLY_ROUTES = ["/login", "/signup"] as const;

/** 비로그인으로 볼 수 있는 경로인지 (PUBLIC_PREFIX_ROUTES에 등록된 경로는 하위 경로도 포함) */
export function isPublicRoute(pathname: string): boolean {
  if (PUBLIC_ROUTES.some((route) => route === pathname)) return true;
  return PUBLIC_PREFIX_ROUTES.some((route) => pathname.startsWith(`${route}/`));
}

/** 로그인 상태로 접근하면 홈으로 보내야 하는 경로인지 */
export function isGuestOnlyRoute(pathname: string): boolean {
  return GUEST_ONLY_ROUTES.some((route) => route === pathname);
}