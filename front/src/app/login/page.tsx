"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("jimin.dev@gmail.com");
  const [password, setPassword] = useState("password");

  // 클라이언트 측 더미 로그인: 세션 플래그를 저장하고 메인으로 이동
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      window.localStorage.setItem("whynago:auth", "1");
    } catch {
      // localStorage 접근 불가 환경은 무시 (더미 동작)
    }
    router.push("/");
  };

  const inputClass =
    "w-full rounded-[11px] border border-[#E0E0DA] bg-[#FAFAF7] px-[15px] py-[13px] text-sm text-[#1C1C1A] outline-none placeholder:text-[#A8A8A0] focus:border-[#1C1C1A]";

  return (
    <div className="flex flex-1 items-center justify-center p-6">
      <form
        onSubmit={handleSubmit}
        className="flex w-full max-w-[400px] flex-col items-center gap-1.5 rounded-[20px] border border-[#ECECE8] bg-white px-9 py-10 shadow-[0_20px_50px_rgba(0,0,0,0.08)]"
      >
        {/* 로고 */}
        <div className="mb-2 flex h-[52px] w-[52px] items-center justify-center rounded-[14px] bg-[#1C1C1A]">
          <span className="font-mono text-[23px] font-bold tracking-[-0.5px] text-white">
            &lt;/&gt;
          </span>
        </div>
        <span className="text-xl font-bold tracking-[-0.4px] text-[#1C1C1A]">
          WhyNaGo
        </span>
        <span className="mb-6 text-[13.5px] text-[#9A9A90]">
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
          className={`${inputClass} mb-[18px]`}
        />

        {/* 액션 */}
        <button
          type="submit"
          className="w-full rounded-[11px] bg-[#1C1C1A] py-[14px] text-[15px] font-semibold text-white transition-colors hover:bg-[#333]"
        >
          로그인
        </button>
        <button
          type="button"
          onClick={() => router.push("/signup")}
          className="mt-[9px] w-full rounded-[11px] border border-[#DCDCD4] bg-white py-[14px] text-[15px] font-semibold text-[#1C1C1A] transition-colors hover:border-[#1C1C1A]"
        >
          회원가입
        </button>
      </form>
    </div>
  );
}
