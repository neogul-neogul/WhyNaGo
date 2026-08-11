"use client";

import type { InterviewHistoryResponse } from "@/types";
import { CATEGORY_LABELS } from "@/lib/questions";
import { formatInterviewDate } from "@/lib/interviews";
import Badge from "@/components/ui/Badge";

// 면접 기록 목록의 카드 — 정답/오답 필터링 없이 완료된 면접을 그대로 보여준다
export default function InterviewHistoryCard({
  history,
  onOpen,
}: {
  history: InterviewHistoryResponse;
  onOpen: () => void;
}) {
  const wrongCount = history.totalCount - history.correctCount;

  return (
    <div
      onClick={onOpen}
      className="flex cursor-pointer items-center justify-between gap-4 rounded-[13px] border border-line-card bg-white px-5 py-[18px] transition-all hover:border-line-strong hover:shadow-[0_2px_10px_rgba(0,0,0,0.04)]"
    >
      <div className="flex min-w-0 items-center gap-3">
        <Badge tone="neutral">{CATEGORY_LABELS[history.category]}</Badge>
        <span className="truncate text-[15px] font-semibold text-ink">{history.title}</span>
      </div>
      <div className="flex flex-shrink-0 items-center gap-4">
        <span className="text-xs font-medium text-placeholder">
          {formatInterviewDate(history.interviewDate)}
        </span>
        <span
          className={`text-[13px] font-bold ${wrongCount === 0 ? "text-success" : "text-secondary"}`}
        >
          통과 {history.correctCount} / {history.totalCount}
        </span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" className="flex-shrink-0 text-icon">
          <path d="M9 6l6 6-6 6" />
        </svg>
      </div>
    </div>
  );
}
