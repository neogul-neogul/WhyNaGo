/**
 * 관리자 화면 전용 세션 (더미).
 *
 * 어드민 백엔드 API가 아직 없어 로그인 검증을 클라이언트에서 처리한다.
 * 실제 사용자 세션(authStorage.ts)과는 완전히 분리되어 있고, 탭을 닫으면 사라진다.
 * 백엔드가 준비되면 이 파일만 apiFetch 기반으로 교체한다.
 */

import { useSyncExternalStore } from "react";

const ADMIN_SESSION_KEY = "whynago:admin";
const ADMIN_EVENT = "whynago:admin-change";

const ADMIN_EMAIL = "admin@admin.admin";
const ADMIN_PASSWORD = "admin1234";

function isBrowser() {
  return typeof window !== "undefined";
}

/** 관리자로 로그인되어 있는지 */
export function isAdminLoggedIn(): boolean {
  if (!isBrowser()) return false;
  try {
    return window.sessionStorage.getItem(ADMIN_SESSION_KEY) === "1";
  } catch {
    return false;
  }
}

function notifyAdminChange() {
  if (!isBrowser()) return;
  window.dispatchEvent(new Event(ADMIN_EVENT));
}

/** 관리자 로그인. 성공하면 true, 계정이 맞지 않으면 false */
export function adminLogin(email: string, password: string): boolean {
  if (email.trim() !== ADMIN_EMAIL || password !== ADMIN_PASSWORD) return false;
  try {
    window.sessionStorage.setItem(ADMIN_SESSION_KEY, "1");
  } catch {
    return false;
  }
  notifyAdminChange();
  return true;
}

/** 관리자 로그아웃 */
export function adminLogout() {
  try {
    window.sessionStorage.removeItem(ADMIN_SESSION_KEY);
  } catch {
    // 저장소 접근이 막혀 있으면 세션도 애초에 없다
  }
  notifyAdminChange();
}

function subscribeAdminChange(callback: () => void) {
  if (!isBrowser()) return () => {};
  window.addEventListener(ADMIN_EVENT, callback);
  return () => window.removeEventListener(ADMIN_EVENT, callback);
}

/**
 * 관리자 로그인 여부를 반응형으로 구독하는 훅.
 * 서버 렌더 시엔 항상 로그아웃(false)으로 렌더해 hydration 불일치를 피한다.
 */
export function useAdminAuth(): boolean {
  return useSyncExternalStore(subscribeAdminChange, isAdminLoggedIn, () => false);
}
