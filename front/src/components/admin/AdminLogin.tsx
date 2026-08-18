"use client";

import { useState } from "react";
import { adminLogin } from "@/lib/adminAuth";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Input from "@/components/ui/Input";
import AdminLogo from "@/components/admin/AdminLogo";

export default function AdminLogin() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const submit = () => {
    if (!adminLogin(email, password)) {
      setError("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-page px-6 py-10">
      <form
        onSubmit={(e) => {
          e.preventDefault();
          submit();
        }}
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

        <Button type="submit" size="xl" className="mt-[18px] w-full">
          로그인
        </Button>

        {error && <p className="mt-3.5 text-[12.5px] text-danger">{error}</p>}

        <p className="mt-[22px] flex items-center gap-[7px] text-xs font-medium text-placeholder">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="9" />
            <path d="M12 8v4M12 16h0" strokeLinecap="round" />
          </svg>
          관리자 권한이 없는 계정은 접근할 수 없습니다.
        </p>
      </form>
    </div>
  );
}
