"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";

export default function SignupPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");

  // 클라이언트 측 더미 회원가입: 세션 플래그를 저장하고 메인으로 이동
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
          몇 가지만 입력하면 시작할 수 있어요
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
          className={`${inputClass} mb-[9px]`}
        />
        <input
          type="text"
          placeholder="닉네임"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          className={`${inputClass} mb-[18px]`}
        />

        {/* 액션 */}
        <button
          type="submit"
          className="w-full rounded-[11px] bg-[#1C1C1A] py-[14px] text-[15px] font-semibold text-white transition-colors hover:bg-[#333]"
        >
          가입하기
        </button>
        <button
          type="button"
          onClick={() => router.push("/login")}
          className="mt-1.5 w-full rounded-[11px] py-3 text-[13.5px] font-medium text-[#6B6B62]"
        >
          이미 계정이 있으신가요?{" "}
          <span className="font-bold text-[#1C1C1A]">로그인</span>
        </button>
      </form>
    </div>
  );
}
