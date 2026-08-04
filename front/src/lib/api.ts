// 백엔드 API 공통 클라이언트
// - base URL 부착, JSON 직렬화, Authorization 헤더 자동 부착
// - access token 만료(401) 시 재발급 후 원 요청 1회 재시도
// - 실패 시 백엔드 에러 형식({ code, message })을 ApiError로 변환해 throw

import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  setTokens,
} from "@/lib/authStorage";
import type { ReissueResponse } from "@/types";

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

/** 재발급까지 실패해 로그인이 필요해졌음을 알리는 이벤트 */
const SESSION_EXPIRED_EVENT = "whynago:session-expired";

/** 백엔드 에러 응답({ code, message })을 담는 예외 */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

interface ApiOptions extends Omit<RequestInit, "body"> {
  /** JSON으로 직렬화할 요청 바디 */
  body?: unknown;
  /** true면 Authorization 헤더를 붙이지 않음 (로그인/회원가입 등) */
  skipAuth?: boolean;
  /** true면 401을 받아도 재발급·재시도를 하지 않음 (재발급 요청 자신의 무한 루프 차단용) */
  noRetry?: boolean;
}

/** 세션 만료를 구독한다 (로그인 화면으로 보내는 처리는 구독자가 담당) */
export function subscribeSessionExpired(callback: () => void) {
  if (typeof window === "undefined") return () => {};
  window.addEventListener(SESSION_EXPIRED_EVENT, callback);
  return () => window.removeEventListener(SESSION_EXPIRED_EVENT, callback);
}

function notifySessionExpired() {
  clearSession();
  if (typeof window === "undefined") return;
  window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
}

let reissuePromise: Promise<string> | null = null;

/**
 * 재발급을 호출하고 새 토큰 쌍을 저장한다.
 * 동시에 여러 요청이 401을 받아도 재발급은 한 번만 나가야 한다.
 * 서버가 rotation(요청마다 이전 refresh token 폐기)을 하므로,
 * 두 번 부르면 두 번째가 무효 토큰으로 실패해 멀쩡한 세션이 끊긴다.
 */
function reissueOnce(refreshToken: string): Promise<string> {
  if (!reissuePromise) {
    reissuePromise = apiFetch<ReissueResponse>("/api/auth/reissue", {
      method: "POST",
      body: { refreshToken },
      skipAuth: true,
      noRetry: true,
    })
      .then((res) => {
        setTokens(res.accessToken, res.refreshToken);
        return res.accessToken;
      })
      .finally(() => {
        reissuePromise = null;
      });
  }
  return reissuePromise;
}

/**
 * 재시도에 쓸 새 access token을 구한다 (실패하면 null).
 *
 * 재발급이 실패했더라도 저장된 refresh token이 시도 시점과 달라졌다면
 * 다른 탭이 먼저 rotation에 성공한 것이므로, 로그아웃시키지 않고
 * 그 탭이 저장해둔 access token으로 재시도한다.
 */
async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;

  try {
    return await reissueOnce(refreshToken);
  } catch {
    const current = getRefreshToken();
    if (current !== null && current !== refreshToken) {
      return getAccessToken();
    }
    return null;
  }
}

export async function apiFetch<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const { body, skipAuth, noRetry, headers, ...rest } = options;
  const payload = body !== undefined ? JSON.stringify(body) : undefined;

  const send = (token: string | null) =>
    fetch(`${BASE_URL}${path}`, {
      ...rest,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...headers,
      },
      ...(payload !== undefined ? { body: payload } : {}),
    });

  const hadSession = getAccessToken() !== null || getRefreshToken() !== null;
  let res = await send(skipAuth ? null : getAccessToken());

  if (res.status === 401 && !skipAuth && !noRetry && hadSession) {
    const token = await refreshAccessToken();
    if (token !== null) {
      res = await send(token);
    }
    if (token === null || res.status === 401) {
      notifySessionExpired();
    }
  }

  if (!res.ok) {
    const errorBody = await res.json().catch(() => ({}));
    throw new ApiError(
      res.status,
      errorBody.code ?? "UNKNOWN",
      errorBody.message ?? "요청을 처리하지 못했습니다.",
    );
  }

  // 204 No Content 등 바디가 없는 응답 처리
  if (res.status === 204) {
    return undefined as T;
  }
  return res.json() as Promise<T>;
}