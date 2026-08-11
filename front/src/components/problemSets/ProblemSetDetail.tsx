"use client";

import type { ProblemSetDetailResponse } from "@/types";
import { CATEGORY_LABELS, DIFFICULTY_LABELS, TYPE_LABELS } from "@/lib/questions";
import { diffColor, lvBadge } from "@/lib/badges";
import Badge, { type BadgeTone } from "@/components/ui/Badge";

const typeTone: Record<string, BadgeTone> = {
  객관식: "accent",
  서술형: "ai",
};

function PlaylistIcon({ size = 22 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 6h11M3 12h11M3 18h7" />
      <path d="M17 11l5 3-5 3z" />
    </svg>
  );
}

// 문제집 상세 — 담긴 문제 표, 순서대로 풀기/제거
export default function ProblemSetDetail({
  problemSet,
  onBack,
  onDelete,
  onRemoveItem,
  onStartQuestion,
  onGoToSolve,
}: {
  problemSet: ProblemSetDetailResponse;
  onBack: () => void;
  onDelete: () => void;
  onRemoveItem: (questionId: number) => void;
  onStartQuestion: (questionId: number) => void;
  onGoToSolve: () => void;
}) {
  const hasItems = problemSet.items.length > 0;

  return (
    <div className="flex max-w-[1000px] flex-col gap-4">
      <div className="flex items-center justify-between gap-3.5">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-[13px] font-semibold text-secondary"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M11 18l-6-6 6-6" />
          </svg>
          문제집 목록
        </button>
        <button
          type="button"
          onClick={onDelete}
          className="rounded-[9px] border border-[#F0D8D4] bg-white px-3.5 py-2 text-[13px] font-semibold text-danger"
        >
          문제집 삭제
        </button>
      </div>

      <div className="flex items-center gap-4 rounded-[16px] border border-line-card bg-white px-6 py-[22px]">
        <div className="flex h-[46px] w-[46px] flex-shrink-0 items-center justify-center rounded-[12px] bg-accent-bg text-accent">
          <PlaylistIcon />
        </div>
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <span className="truncate text-[18px] font-bold tracking-[-0.3px] text-ink">
            {problemSet.name}
          </span>
          <span className="text-[13px] text-soft">{problemSet.items.length}문제</span>
        </div>
        {hasItems && (
          <button
            type="button"
            onClick={() => onStartQuestion(problemSet.items[0].questionId)}
            className="rounded-[10px] bg-ink px-5 py-[11px] text-[14px] font-semibold text-white transition-colors hover:bg-ink-hover"
          >
            첫 문제부터 풀기
          </button>
        )}
      </div>

      {hasItems && (
        <div className="overflow-hidden rounded-[16px] border border-line-card bg-white">
          <div className="flex items-center gap-4 border-b border-line-card bg-subtle px-[22px] py-[13px] text-xs font-semibold text-placeholder">
            <span className="w-7 flex-shrink-0 text-center">#</span>
            <span className="min-w-0 flex-1">제목</span>
            <span className="w-[70px] flex-shrink-0 text-center">유형</span>
            <span className="w-[60px] flex-shrink-0 text-center">난이도</span>
            <span className="w-[52px] flex-shrink-0" />
          </div>

          {problemSet.items.map((item, i) => {
            const diffLabel = DIFFICULTY_LABELS[item.difficulty];
            const typeLabel = TYPE_LABELS[item.type];
            return (
              <div
                key={item.questionId}
                className="flex items-center gap-4 border-b border-line-soft px-[22px] py-[15px] last:border-b-0"
              >
                <button
                  type="button"
                  onClick={() => onStartQuestion(item.questionId)}
                  className="w-7 flex-shrink-0 text-center font-mono text-[13px] font-semibold text-placeholder"
                >
                  {i + 1}
                </button>
                <button
                  type="button"
                  onClick={() => onStartQuestion(item.questionId)}
                  className="flex min-w-0 flex-1 flex-col gap-[3px] text-left"
                >
                  <span className="truncate text-[14.5px] font-semibold text-ink">{item.title}</span>
                  <span className="text-[12px] text-soft">{CATEGORY_LABELS[item.category]}</span>
                </button>
                <span className="flex w-[70px] flex-shrink-0 justify-center">
                  <Badge tone={typeTone[typeLabel] ?? "neutral"}>{typeLabel}</Badge>
                </span>
                <span
                  className="w-[60px] flex-shrink-0 text-center text-[12.5px] font-bold"
                  style={{ color: diffColor(diffLabel) }}
                >
                  {lvBadge(diffLabel)}
                </span>
                <span className="flex w-[52px] flex-shrink-0 justify-end">
                  <button
                    type="button"
                    onClick={() => onRemoveItem(item.questionId)}
                    className="p-1 text-xs font-semibold text-placeholder transition-colors hover:text-danger"
                  >
                    제거
                  </button>
                </span>
              </div>
            );
          })}
        </div>
      )}

      {!hasItems && (
        <div className="flex flex-col items-center gap-2 rounded-[16px] border border-dashed border-[#DEDED6] px-6 py-[52px] text-center">
          <span className="text-[14.5px] font-semibold text-ink">아직 담은 문제가 없습니다</span>
          <span className="text-[13px] text-soft">
            문제 풀이 화면 오른쪽 위의 <b>문제집에 저장</b>으로 담을 수 있어요
          </span>
          <button
            type="button"
            onClick={onGoToSolve}
            className="mt-2.5 rounded-[10px] bg-ink px-5 py-2.5 text-[13.5px] font-semibold text-white transition-colors hover:bg-ink-hover"
          >
            문제 풀러 가기
          </button>
        </div>
      )}
    </div>
  );
}
