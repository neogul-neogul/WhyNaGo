/**
 * 인증 도메인 API와 로그인 상태 구독 훅.
 * 저장소 접근은 authStorage.ts에 위임하고, 여기서는 흐름만 다룬다.
 */

import { useSyncExternalStore } from "react";
import { apiFetch } from "@/lib/api";
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  getStoredUser,
  notifyAuthChange,
  setStoredUser,
  setTokens,
  subscribeAuthChange,
} from "@/lib/authStorage";
import { resetStreak } from "@/lib/streakStore";
import type {
  AuthUser,
  LoginResponse,
  Role,
  SignUpResponse,
  UserProfileResponse,
} from "@/types";

/**
 * 로그인 자체는 성공했지만 그 화면에서 받을 수 없는 권한일 때 던진다.
 * 관리자 계정은 /admin에서만, 일반 계정은 /login에서만 로그인할 수 있다.
 */
export class RoleMismatchError extends Error {
  constructor(public readonly role: Role) {
    super("이 화면에서 로그인할 수 없는 계정입니다.");
    this.name = "RoleMismatchError";
  }
}

/** 현재 로그인 여부 (저장된 access token 존재 여부로 판단) */
export function isLoggedIn(): boolean {
  return getAccessToken() !== null;
}

/** 현재 로그인한 사용자가 관리자인지 (저장된 사용자 정보 기준) */
export function isAdmin(): boolean {
  return isLoggedIn() && getStoredUser()?.role === "ADMIN";
}

/** 저장된 사용자 정보 (없으면 null) */
export function getCurrentUser(): AuthUser | null {
  return getStoredUser();
}

/** 로그인 성공 응답을 받아 토큰과 사용자 정보를 저장하고 구독자에게 통지한다 */
export function saveSession(res: LoginResponse) {
  setTokens(res.accessToken, res.refreshToken);
  setStoredUser({
    id: res.id,
    email: res.email,
    nickname: res.nickname,
    position: res.position,
    role: res.role,
  });
  notifyAuthChange();
}

/**
 * 기대한 권한이면 세션을 저장하고, 아니면 발급받은 refresh token을 폐기한 뒤 던진다.
 * 권한이 맞지 않는 응답은 저장 자체를 하지 않으므로 그 화면에 로그인 흔적이 남지 않는다.
 */
function saveSessionForRole(res: LoginResponse, expected: Role): LoginResponse {
  if (res.role !== expected) {
    void revokeRefreshToken(res.refreshToken);
    throw new RoleMismatchError(res.role);
  }
  saveSession(res);
  return res;
}

/**
 * 서버에 저장된 refresh token 폐기를 요청한다.
 * 실패는 삼킨다 — 폐기에 실패해도 되돌릴 수단이 없고, 클라이언트는 이미 토큰을 버린 상태다.
 */
async function revokeRefreshToken(refreshToken: string) {
  try {
    await apiFetch<void>("/api/auth/logout", {
      method: "POST",
      body: { refreshToken },
      skipAuth: true,
    });
  } catch {
    return;
  }
}

/**
 * 로그아웃: 로컬 세션을 먼저 비우고, 서버에 저장된 refresh token 폐기를 이어서 요청한다.
 * 로컬 정리가 첫 await 이전에 끝나므로 호출부는 기다리지 않고 화면을 전환해도 된다.
 * 서버 호출 실패는 삼킨다 — 폐기에 실패해도 되돌릴 수단이 없고,
 * 로컬 세션을 남기면 사용자에겐 로그아웃이 동작하지 않는 것으로 보인다.
 */
export async function logout() {
  const refreshToken = getRefreshToken();
  clearSession();
  resetStreak();
  notifyAuthChange();

  if (refreshToken === null) return;
  await revokeRefreshToken(refreshToken);
}

/**
 * 서버에서 받은 최신 프로필로 저장된 사용자 정보를 갱신한다.
 * 재발급 응답에는 사용자 정보가 없어(백엔드 002 D7) 프로필 조회·수정 시점에 맞춰 동기화한다.
 * 프로필 응답에는 id·role이 없으므로 저장된 값을 유지한다.
 */
export function syncStoredUser(profile: UserProfileResponse) {
  const current = getStoredUser();
  if (current === null) return;
  setStoredUser({
    id: current.id,
    email: profile.email,
    nickname: profile.nickname,
    position: profile.position,
    role: current.role,
  });
  notifyAuthChange();
}

/**
 * 로그인 API 호출 후 성공 시 세션 저장.
 * expectedRole과 다른 권한이면 세션을 만들지 않고 RoleMismatchError를 던진다.
 */
export async function requestLogin(
  email: string,
  password: string,
  expectedRole: Role,
): Promise<LoginResponse> {
  const res = await apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: { email, password },
    skipAuth: true,
  });
  return saveSessionForRole(res, expectedRole);
}

/**
 * 구글 로그인 API 호출 후 성공 시 세션 저장.
 * credential은 GIS가 콜백으로 준 id_token이며, 검증은 백엔드가 한다(저장하지 않는다).
 */
export async function requestGoogleLogin(
  credential: string,
  expectedRole: Role,
): Promise<LoginResponse> {
  const res = await apiFetch<LoginResponse>("/api/auth/login/google", {
    method: "POST",
    body: { credential },
    skipAuth: true,
  });
  return saveSessionForRole(res, expectedRole);
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

/**
 * 로그인 여부를 반응형으로 구독하는 훅.
 * 서버 렌더 시엔 항상 로그아웃(false)으로 렌더해 hydration 불일치를 피한다.
 */
export function useAuth(): boolean {
  return useSyncExternalStore(subscribeAuthChange, isLoggedIn, () => false);
}

/** 저장된 사용자 정보를 반응형으로 구독하는 훅 (서버 렌더 시 null) */
export function useCurrentUser(): AuthUser | null {
  return useSyncExternalStore(subscribeAuthChange, getCurrentUser, () => null);
}

/** 관리자 여부를 반응형으로 구독하는 훅 (서버 렌더 시 false) */
export function useIsAdmin(): boolean {
  return useSyncExternalStore(subscribeAuthChange, isAdmin, () => false);
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
