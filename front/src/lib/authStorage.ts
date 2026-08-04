/**
 * 로그인 세션(토큰 + 사용자 정보)의 localStorage 접근을 전담한다.
 * 토큰 키 문자열과 인증 변경 이벤트는 이 파일에만 정의하고,
 * api.ts·auth.ts는 여기를 거쳐서만 저장소에 접근한다.
 */

import type { AuthUser } from "@/types";

const ACCESS_TOKEN_KEY = "whynago:accessToken";
const REFRESH_TOKEN_KEY = "whynago:refreshToken";
const USER_KEY = "whynago:user";
const AUTH_EVENT = "whynago:auth-change";

function isBrowser() {
  return typeof window !== "undefined";
}

function read(key: string): string | null {
  if (!isBrowser()) return null;
  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string) {
  if (!isBrowser()) return;
  try {
    window.localStorage.setItem(key, value);
  } catch {
    return;
  }
}

function remove(key: string) {
  if (!isBrowser()) return;
  try {
    window.localStorage.removeItem(key);
  } catch {
    return;
  }
}

/** 저장된 access token (없거나 서버 렌더 중이면 null) */
export function getAccessToken(): string | null {
  return read(ACCESS_TOKEN_KEY);
}

/** 저장된 refresh token (없거나 서버 렌더 중이면 null) */
export function getRefreshToken(): string | null {
  return read(REFRESH_TOKEN_KEY);
}

/** 토큰 쌍을 저장한다 (로그인·재발급 공용) */
export function setTokens(accessToken: string, refreshToken: string) {
  write(ACCESS_TOKEN_KEY, accessToken);
  write(REFRESH_TOKEN_KEY, refreshToken);
}

// getStoredUser가 매번 새 객체를 반환하면 useSyncExternalStore가 무한 렌더링하므로,
// 원본 문자열이 바뀔 때만 다시 파싱해 참조를 안정적으로 유지한다.
let cachedUserRaw: string | null = null;
let cachedUser: AuthUser | null = null;

/** 저장된 사용자 정보 (없으면 null). 같은 원본에 대해 항상 같은 참조를 반환한다 */
export function getStoredUser(): AuthUser | null {
  const raw = read(USER_KEY);
  if (raw === cachedUserRaw) return cachedUser;
  cachedUserRaw = raw;
  try {
    cachedUser = raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    cachedUser = null;
  }
  return cachedUser;
}

/** 사용자 정보를 저장한다 */
export function setStoredUser(user: AuthUser) {
  write(USER_KEY, JSON.stringify(user));
}

/** 토큰과 사용자 정보를 모두 제거한다 (로그아웃·세션 만료 공용) */
export function clearSession() {
  remove(ACCESS_TOKEN_KEY);
  remove(REFRESH_TOKEN_KEY);
  remove(USER_KEY);
}

/** 인증 상태가 바뀌었음을 같은 탭의 구독자에게 통지한다 */
export function notifyAuthChange() {
  if (!isBrowser()) return;
  window.dispatchEvent(new Event(AUTH_EVENT));
}

/**
 * 인증 상태 변경을 구독한다.
 * 같은 탭의 변경은 커스텀 이벤트로, 다른 탭의 변경은 storage 이벤트로 전달된다.
 */
export function subscribeAuthChange(callback: () => void) {
  if (!isBrowser()) return () => {};
  window.addEventListener(AUTH_EVENT, callback);
  window.addEventListener("storage", callback);
  return () => {
    window.removeEventListener(AUTH_EVENT, callback);
    window.removeEventListener("storage", callback);
  };
}