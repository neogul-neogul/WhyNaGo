"use client";

import Link from "next/link";

// 풀이 결과 요약
export default function QuizResult({
  type,
  correct,
  total,
  onRestart,
}: {
  type: "객관식" | "서술형";
  correct: number;
  total: number;
  onRestart: () => void;
}) {
  const isMultipleChoice = type === "객관식";
  const rate = total > 0 ? Math.round((correct / total) * 100) : 0;
  const wrong = Math.max(0, total - correct);

  return (
    <div className="flex max-w-[620px] flex-col gap-5">
      <div className="flex flex-col items-center gap-2 rounded-[16px] border border-[#ECECE8] bg-white p-[34px] text-center">
        <div className="mb-1.5 flex h-14 w-14 items-center justify-center rounded-full bg-[#E8F5EE]">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#16A34A" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20 6L9 17l-5-5" />
          </svg>
        </div>
        <div className="text-[19px] font-bold text-[#1C1C1A]">풀이 완료 · 결과 저장됨</div>
        <div className="text-[13.5px] text-[#9A9A90]">학습 기록과 진척도에 반영되었습니다</div>
        <div className="mt-[18px] flex w-full overflow-hidden rounded-[14px] border border-[#ECECE8]">
          <div className="flex flex-1 flex-col gap-1 border-r border-[#ECECE8] p-5">
            <span className="font-mono text-[30px] font-bold text-[#1C1C1A]">{rate}%</span>
            <span className="text-xs text-[#9A9A90]">{isMultipleChoice ? "정답률" : "이해율"}</span>
          </div>
          <div className="flex flex-1 flex-col gap-1 border-r border-[#ECECE8] p-5">
            <span className="font-mono text-[30px] font-bold text-[#16A34A]">{correct}</span>
            <span className="text-xs text-[#9A9A90]">{isMultipleChoice ? "정답" : "이해 완료"}</span>
          </div>
          <div className="flex flex-1 flex-col gap-1 p-5">
            <span className="font-mono text-[30px] font-bold text-[#DC2626]">{wrong}</span>
            <span className="text-xs text-[#9A9A90]">{isMultipleChoice ? "오답" : "복습 필요"}</span>
          </div>
        </div>
      </div>
      <div className="flex gap-3">
        <button
          type="button"
          onClick={onRestart}
          className="flex-1 rounded-[11px] border border-[#DCDCD4] bg-white p-[13px] text-[14.5px] font-semibold text-[#1C1C1A]"
        >
          다시 풀기
        </button>
        <Link
          href="/wrong"
          className="flex-1 rounded-[11px] bg-[#1C1C1A] p-[13px] text-center text-[14.5px] font-semibold text-white"
        >
          오답 복습하기
        </Link>
      </div>
    </div>
  );
}
