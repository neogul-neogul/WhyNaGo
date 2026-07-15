// 백엔드 API 공통 클라이언트
// - base URL 부착, JSON 직렬화, Authorization 헤더 자동 부착
// - 실패 시 백엔드 에러 형식({ code, message })을 ApiError로 변환해 throw

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

const ACCESS_TOKEN_KEY = "whynago:accessToken";

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

function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  try {
    return window.localStorage.getItem(ACCESS_TOKEN_KEY);
  } catch {
    return null;
  }
}

interface ApiOptions extends Omit<RequestInit, "body"> {
  /** JSON으로 직렬화할 요청 바디 */
  body?: unknown;
  /** true면 Authorization 헤더를 붙이지 않음 (로그인/회원가입 등) */
  skipAuth?: boolean;
}

export async function apiFetch<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const { body, skipAuth, headers, ...rest } = options;
  const token = skipAuth ? null : getAccessToken();

  const res = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });

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