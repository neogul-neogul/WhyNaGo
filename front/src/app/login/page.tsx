"use client";

import { Suspense, useCallback, useRef, useState, type FormEvent } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Script from "next/script";
import { RoleMismatchError, requestGoogleLogin, requestLogin } from "@/lib/auth";
import { ApiError } from "@/lib/api";
import AuthCard from "@/components/auth/AuthCard";
import Input from "@/components/ui/Input";

const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ?? "";

// AuthCard 내부 폭(max-w-400 - px-9 양쪽)에 맞춘다. GIS 버튼은 px 단위만 받는다
const GOOGLE_BUTTON_WIDTH = 328;

// 관리자 계정은 관리자 페이지에서만 로그인할 수 있다 (권한이 다르면 세션을 만들지 않는다)
const ADMIN_ACCOUNT_MESSAGE = "관리자 계정입니다. 관리자 페이지에서 로그인해주세요.";


// 외부 URL로 튕기지 않도록 앱 내부 경로만 복귀 대상으로 인정한다
function safeRedirect(target: string | null): string {
  if (target === null) return "/";
  if (!target.startsWith("/") || target.startsWith("//")) return "/";
  return target;
}

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirect = safeRedirect(searchParams.get("redirect"));
  const expired = searchParams.get("reason") === "expired";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const googleButtonRef = useRef<HTMLDivElement>(null);

  // 백엔드에 로그인 요청 → 성공 시 세션 저장 후 원래 보던 화면으로 복귀, 실패 시 에러 표시
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await requestLogin(email, password, "USER");
      router.replace(redirect);
    } catch (err) {
      if (err instanceof RoleMismatchError) {
        setError(ADMIN_ACCOUNT_MESSAGE);
      } else if (err instanceof ApiError && err.code === "AUTH_LOGIN_FAILED") {
        setError("이메일 또는 비밀번호가 올바르지 않습니다.");
      } else if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("로그인에 실패했습니다. 잠시 후 다시 시도해주세요.");
      }
    } finally {
      setLoading(false);
    }
  };

  // GIS가 넘겨준 id_token을 백엔드로 보낸다. 계정 중복 등 안내 문구는 백엔드 메시지를 그대로 쓴다
  const handleGoogleCredential = useCallback(
    async (credential: string) => {
      setError("");
      try {
        await requestGoogleLogin(credential, "USER");
        router.replace(redirect);
      } catch (err) {
        if (err instanceof RoleMismatchError) {
          setError(ADMIN_ACCOUNT_MESSAGE);
        } else if (err instanceof ApiError) {
          setError(err.message);
        } else {
          setError("구글 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
      }
    },
    [redirect, router],
  );

  // 스크립트 로드 후와 재마운트 때마다 호출된다(next/script onReady)
  const renderGoogleButton = useCallback(() => {
    const google = window.google;
    if (google === undefined || googleButtonRef.current === null) return;

    google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: (response) => {
        void handleGoogleCredential(response.credential);
      },
    });
    google.accounts.id.renderButton(googleButtonRef.current, {
      type: "standard",
      theme: "outline",
      size: "large",
      text: "continue_with",
      shape: "rectangular",
      logo_alignment: "center",
      width: GOOGLE_BUTTON_WIDTH,
    });
  }, [handleGoogleCredential]);

  return (
    <AuthCard subtitle="WhyNaGo에서 성장해보세요!" onSubmit={handleSubmit}>
      {/* 세션 만료 안내 */}
      {expired && (
        <p className="mb-[9px] w-full rounded-[9px] bg-accent-faint px-3 py-2.5 text-[13px] text-accent">
          세션이 만료되었어요. 다시 로그인해주세요.
        </p>
      )}

      {/* 입력 */}
      <Input
        type="email"
        placeholder="이메일"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        className="mb-[9px]"
      />
      <Input
        type="password"
        placeholder="비밀번호"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        className={error ? "mb-[9px]" : "mb-[18px]"}
      />

      {/* 에러 메시지 */}
      {error && (
        <p className="mb-[9px] w-full text-[13px] text-danger">{error}</p>
      )}

      {/* 액션 */}
      <button
        type="submit"
        disabled={loading}
        className="w-full rounded-[11px] bg-ink py-[14px] text-[15px] font-semibold text-white transition-colors hover:bg-ink-hover disabled:cursor-not-allowed disabled:opacity-60"
      >
        {loading ? "로그인 중..." : "로그인"}
      </button>
      <button
        type="button"
        onClick={() => router.push("/signup")}
        className="mt-[9px] w-full rounded-[11px] border border-line-strong bg-white py-[14px] text-[15px] font-semibold text-ink transition-colors hover:border-ink"
      >
        회원가입
      </button>

      {/* 구글 로그인 — client id가 없으면 렌더하지 않는다 */}
      {GOOGLE_CLIENT_ID !== "" && (
        <>
          <div className="my-[18px] flex w-full items-center gap-3">
            <span className="h-px flex-1 bg-line-strong" />
            <span className="text-[12.5px] text-soft">또는</span>
            <span className="h-px flex-1 bg-line-strong" />
          </div>
          <div ref={googleButtonRef} className="flex w-full justify-center" />
          <Script
            src="https://accounts.google.com/gsi/client"
            strategy="afterInteractive"
            onReady={renderGoogleButton}
          />
        </>
      )}
    </AuthCard>
  );
}

// useSearchParams는 정적 프리렌더에서 CSR bailout을 일으키므로 Suspense 경계가 필요하다
export default function LoginPage() {
  return (
    <Suspense fallback={null}>
      <LoginForm />
    </Suspense>
  );
}