"use client";

import Link from "next/link";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

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
      <Card className="flex flex-col items-center gap-2 p-[34px] text-center">
        <div className="mb-1.5 flex h-14 w-14 items-center justify-center rounded-full bg-success-pale text-success">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20 6L9 17l-5-5" />
          </svg>
        </div>
        <div className="text-[19px] font-bold text-ink">풀이 완료 · 결과 저장됨</div>
        <div className="text-[13.5px] text-soft">학습 기록과 진척도에 반영되었습니다</div>
        <div className="mt-[18px] flex w-full overflow-hidden rounded-[14px] border border-line-card">
          <div className="flex flex-1 flex-col gap-1 border-r border-line-card p-5">
            <span className="font-mono text-[30px] font-bold text-ink">{rate}%</span>
            <span className="text-xs text-soft">{isMultipleChoice ? "정답률" : "이해율"}</span>
          </div>
          <div className="flex flex-1 flex-col gap-1 border-r border-line-card p-5">
            <span className="font-mono text-[30px] font-bold text-success">{correct}</span>
            <span className="text-xs text-soft">{isMultipleChoice ? "정답" : "이해 완료"}</span>
          </div>
          <div className="flex flex-1 flex-col gap-1 p-5">
            <span className="font-mono text-[30px] font-bold text-danger">{wrong}</span>
            <span className="text-xs text-soft">{isMultipleChoice ? "오답" : "복습 필요"}</span>
          </div>
        </div>
      </Card>
      <div className="flex gap-3">
        <Button variant="secondary" size="xl" onClick={onRestart} className="flex-1">
          다시 풀기
        </Button>
        <Link
          href="/wrong"
          className="flex-1 rounded-[11px] bg-ink p-[13px] text-center text-[15px] font-semibold text-white transition-colors hover:bg-ink-hover"
        >
          오답 복습하기
        </Link>
      </div>
    </div>
  );
}
