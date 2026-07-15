import { useSyncExternalStore } from "react";
import { apiFetch } from "@/lib/api";
import type { AuthUser, LoginResponse, SignUpResponse } from "@/types";

// 로그인 세션(토큰 + 사용자 정보)을 localStorage에 저장/조회/삭제한다.
const ACCESS_TOKEN_KEY = "whynago:accessToken";
const REFRESH_TOKEN_KEY = "whynago:refreshToken";
const USER_KEY = "whynago:user";
const AUTH_EVENT = "whynago:auth-change";

function isBrowser() {
  return typeof window !== "undefined";
}

/** 현재 로그인 여부 (저장된 accessToken 존재 여부로 판단) */
export function isLoggedIn(): boolean {
  if (!isBrowser()) return false;
  try {
    return window.localStorage.getItem(ACCESS_TOKEN_KEY) !== null;
  } catch {
    return false;
  }
}

// getCurrentUser가 매번 새 객체를 반환하면 useSyncExternalStore가 무한 렌더링하므로,
// 원본 문자열이 바뀔 때만 다시 파싱해 참조를 안정적으로 유지한다.
let cachedUserRaw: string | null = null;
let cachedUser: AuthUser | null = null;

/** 저장된 사용자 정보 (없으면 null) */
export function getCurrentUser(): AuthUser | null {
  if (!isBrowser()) return null;
  let raw: string | null;
  try {
    raw = window.localStorage.getItem(USER_KEY);
  } catch {
    return null;
  }
  if (raw === cachedUserRaw) return cachedUser;
  cachedUserRaw = raw;
  try {
    cachedUser = raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    cachedUser = null;
  }
  return cachedUser;
}

/** 로그인 성공 응답을 받아 토큰과 사용자 정보를 저장하고 구독자에게 통지 */
export function saveSession(res: LoginResponse) {
  if (!isBrowser()) return;
  try {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, res.accessToken);
    window.localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
    const user: AuthUser = {
      id: res.id,
      email: res.email,
      nickname: res.nickname,
      position: res.position,
    };
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  } catch {
    // 저장 불가 환경 무시
  }
  window.dispatchEvent(new Event(AUTH_EVENT));
}

/** 로그아웃: 저장된 세션 정보를 모두 제거하고 구독자에게 통지 */
export function logout() {
  if (!isBrowser()) return;
  try {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
  } catch {
    // 삭제 불가 환경 무시
  }
  window.dispatchEvent(new Event(AUTH_EVENT));
}

/** 로그인 API 호출 후 성공 시 세션 저장 */
export async function requestLogin(email: string, password: string): Promise<LoginResponse> {
  const res = await apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: { email, password },
    skipAuth: true,
  });
  saveSession(res);
  return res;
}

/** 회원가입 API 호출 (세션 저장은 하지 않음 — 성공 후 로그인 페이지로 이동) */
export async function requestSignup(
  email: string,
  password: string,
  nickname: string,
): Promise<SignUpResponse> {
  return apiFetch<SignUpResponse>("/api/auth/signup", {
    method: "POST",
    body: { email, password, nickname },
    skipAuth: true,
  });
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

/** 저장된 사용자 정보를 반응형으로 구독하는 훅 (서버 렌더 시 null) */
export function useCurrentUser(): AuthUser | null {
  return useSyncExternalStore(subscribe, getCurrentUser, () => null);
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
