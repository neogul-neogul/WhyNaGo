export const COMING_SOON_ROUTES = [
  "/mock",
  "/weekly",
] as const;

export function isComingSoon(pathname: string): boolean {
  return COMING_SOON_ROUTES.some(
    (route) => pathname === route || pathname.startsWith(`${route}/`),
  );
}
