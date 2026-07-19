"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { requestSignup } from "@/lib/auth";
import { ApiError } from "@/lib/api";
import AuthCard from "@/components/auth/AuthCard";
import Input from "@/components/ui/Input";

export default function SignupPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // 백엔드에 회원가입 요청 → 성공 시 로그인 페이지로 이동, 실패 시 에러 표시
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await requestSignup(email, password, nickname);
      router.push("/login");
    } catch (err) {
      if (err instanceof ApiError && err.code === "USER_DUPLICATE_EMAIL") {
        setError("이미 사용 중인 이메일입니다.");
      } else if (err instanceof ApiError && err.code === "USER_DUPLICATE_NICKNAME") {
        setError("이미 사용 중인 닉네임입니다.");
      } else if (err instanceof ApiError && err.status === 400) {
        setError("입력 형식을 확인해주세요. (비밀번호 8~12자, 닉네임 4~8자)");
      } else if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("회원가입에 실패했습니다. 잠시 후 다시 시도해주세요.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthCard subtitle="몇 가지만 입력하면 시작할 수 있어요" onSubmit={handleSubmit}>
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
        className="mb-[9px]"
      />
      <Input
        type="text"
        placeholder="닉네임"
        value={nickname}
        onChange={(e) => setNickname(e.target.value)}
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
        {loading ? "가입 중..." : "가입하기"}
      </button>
      <button
        type="button"
        onClick={() => router.push("/login")}
        className="mt-1.5 w-full rounded-[11px] py-3 text-[13.5px] font-medium text-secondary"
      >
        이미 계정이 있으신가요?{" "}
        <span className="font-bold text-ink">로그인</span>
      </button>
    </AuthCard>
  );
}
