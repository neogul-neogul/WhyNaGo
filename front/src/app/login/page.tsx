"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { requestLogin } from "@/lib/auth";
import { ApiError } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // 백엔드에 로그인 요청 → 성공 시 세션 저장 후 메인으로 이동, 실패 시 에러 표시
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await requestLogin(email, password);
      router.push("/");
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

  const inputClass =
    "w-full rounded-[11px] border border-line-input bg-subtle px-[15px] py-[13px] text-sm text-ink outline-none placeholder:text-placeholder focus:border-ink";

  return (
    <div className="flex flex-1 items-center justify-center p-6">
      <form
        onSubmit={handleSubmit}
        className="flex w-full max-w-[400px] flex-col items-center gap-1.5 rounded-[20px] border border-line-card bg-white px-9 py-10 shadow-[0_20px_50px_rgba(0,0,0,0.08)]"
      >
        {/* 로고 */}
        <div className="mb-2 flex h-[52px] w-[52px] items-center justify-center rounded-[14px] bg-ink">
          <span className="font-mono text-[23px] font-bold tracking-[-0.5px] text-white">
            &lt;/&gt;
          </span>
        </div>
        <span className="text-xl font-bold tracking-[-0.4px] text-ink">
          WhyNaGo
        </span>
        <span className="mb-6 text-[13.5px] text-soft">
          WhyNaGo에서 성장해보세요!
        </span>

        {/* 입력 */}
        <input
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className={`${inputClass} mb-[9px]`}
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className={`${inputClass} ${error ? "mb-[9px]" : "mb-[18px]"}`}
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
      </form>
    </div>
  );
}
