"use client";

import { Suspense, useState, type FormEvent } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { requestLogin } from "@/lib/auth";
import { ApiError } from "@/lib/api";
import AuthCard from "@/components/auth/AuthCard";
import Input from "@/components/ui/Input";

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

  // 백엔드에 로그인 요청 → 성공 시 세션 저장 후 원래 보던 화면으로 복귀, 실패 시 에러 표시
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await requestLogin(email, password);
      router.replace(redirect);
    } catch (err) {
      if (err instanceof ApiError && err.code === "AUTH_LOGIN_FAILED") {
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