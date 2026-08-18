"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { ApiError } from "@/lib/api";
import { RoleMismatchError, logout, requestLogin, useCurrentUser } from "@/lib/auth";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Input from "@/components/ui/Input";
import AdminLogo from "@/components/admin/AdminLogo";

/**
 * 관리자 로그인.
 * 사용자 화면과 같은 로그인 API를 쓰되 role이 ADMIN인 계정만 받는다 — 일반 계정으로는
 * 인증에 성공해도 세션을 만들지 않고 발급받은 토큰을 즉시 폐기한다.
 */
export default function AdminLogin() {
  const user = useCurrentUser();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await requestLogin(email, password, "ADMIN");
    } catch (err) {
      if (err instanceof RoleMismatchError) {
        setError("관리자 권한이 없는 계정입니다.");
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

  return (
    <div className="flex min-h-screen items-center justify-center bg-page px-6 py-10">
      <form
        onSubmit={submit}
        className="flex w-full max-w-[440px] flex-col items-center rounded-[20px] bg-white p-12 shadow-[0_20px_50px_rgba(0,0,0,0.06)]"
      >
        <AdminLogo size="lg" />

        <div className="mt-[22px] flex items-center gap-[11px]">
          <span className="text-2xl font-extrabold tracking-[-0.6px] text-ink">WhyNaGo</span>
          <Badge tone="neutral" className="tracking-[0.4px]">
            ADMIN
          </Badge>
        </div>
        <p className="mt-2.5 text-[13px] font-medium text-soft">관리자 전용 페이지입니다.</p>

        {/* 일반 계정으로 로그인한 채 들어온 경우 — 관리자 계정으로 다시 로그인해야 한다 */}
        {user !== null && (
          <div className="mt-6 flex w-full flex-col items-start gap-2 rounded-[11px] bg-neutral px-4 py-3.5">
            <span className="text-[13px] font-medium text-secondary">
              {user.nickname} 님(일반 계정)으로 로그인되어 있습니다.
            </span>
            <button
              type="button"
              onClick={() => logout()}
              className="cursor-pointer text-[13px] font-semibold text-ink underline underline-offset-2"
            >
              로그아웃
            </button>
          </div>
        )}

        <div className="mt-[34px] flex w-full flex-col gap-[9px]">
          <Input
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              setError("");
            }}
            placeholder="이메일"
            autoComplete="username"
          />
          <Input
            type="password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              setError("");
            }}
            placeholder="비밀번호"
            autoComplete="current-password"
          />
        </div>

        <Button type="submit" size="xl" disabled={loading} className="mt-[18px] w-full">
          {loading ? "로그인 중..." : "로그인"}
        </Button>

        {error && <p className="mt-3.5 text-[12.5px] text-danger">{error}</p>}

        <p className="mt-[22px] flex items-center gap-[7px] text-xs font-medium text-placeholder">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="9" />
            <path d="M12 8v4M12 16h0" strokeLinecap="round" />
          </svg>
          관리자 권한이 없는 계정은 접근할 수 없습니다.
        </p>

        <Link
          href="/login"
          className="mt-3 text-xs font-medium text-secondary underline underline-offset-2 transition-colors hover:text-ink"
        >
          일반 사용자 로그인
        </Link>
      </form>
    </div>
  );
}
