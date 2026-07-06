import { useSyncExternalStore } from "react";

// 클라이언트 측 더미 인증 상태 (실제 토큰/세션 없음)
const AUTH_KEY = "whynago:auth";
const AUTH_EVENT = "whynago:auth-change";

function isBrowser() {
  return typeof window !== "undefined";
}

/** 현재 로그인 여부 (기본값: 로그아웃) */
export function isLoggedIn(): boolean {
  if (!isBrowser()) return false;
  try {
    return window.localStorage.getItem(AUTH_KEY) === "1";
  } catch {
    // localStorage 접근 불가 환경은 로그아웃으로 간주
    return false;
  }
}

/** 더미 로그인: 세션 플래그 저장 후 구독자에게 변경 통지 */
export function login() {
  if (!isBrowser()) return;
  try {
    window.localStorage.setItem(AUTH_KEY, "1");
  } catch {
    // 저장 불가 환경 무시 (더미 동작)
  }
  window.dispatchEvent(new Event(AUTH_EVENT));
}

/** 더미 로그아웃: 세션 플래그 제거 후 구독자에게 변경 통지 */
export function logout() {
  if (!isBrowser()) return;
  try {
    window.localStorage.removeItem(AUTH_KEY);
  } catch {
    // 삭제 불가 환경 무시 (더미 동작)
  }
  window.dispatchEvent(new Event(AUTH_EVENT));
}

function subscribe(callback: () => void) {
  if (!isBrowser()) return () => {};
  window.addEventListener(AUTH_EVENT, callback);
  window.addEventListener("storage", callback);
  return () => {
    window.removeEventListener(AUTH_EVENT, callback);
    window.removeEventListener("storage", callback);
  };
}

/**
 * 로그인 여부를 반응형으로 구독하는 훅.
 * 서버 렌더 시엔 항상 로그아웃(false)으로 렌더해 hydration 불일치를 피한다.
 */
export function useAuth(): boolean {
  return useSyncExternalStore(subscribe, isLoggedIn, () => false);
}

const noopSubscribe = () => () => {};

/**
 * 클라이언트에서 hydration이 끝났는지 여부.
 * 서버/첫 hydration 렌더에선 false, 이후 클라이언트에서 true를 반환한다.
 * 인증 상태가 확정되기 전(false)에는 로그인/프로필 UI 확정을 미뤄 깜빡임을 막는다.
 */
export function useHydrated(): boolean {
  return useSyncExternalStore(
    noopSubscribe,
    () => true,
    () => false,
  );
}
