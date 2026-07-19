"use client";

import type { WrongNote } from "@/types";
import { diffColor } from "@/lib/badges";
import Button from "@/components/ui/Button";

// 오답노트 목록의 문제 카드 (북마크/삭제/재풀이 액션 포함)
export default function WrongNoteCard({
  note,
  bookmarked,
  onOpen,
  onToggleBookmark,
  onDelete,
  onResolve,
}: {
  note: WrongNote;
  bookmarked: boolean;
  onOpen: () => void;
  onToggleBookmark: () => void;
  onDelete: () => void;
  onResolve: () => void;
}) {
  return (
    <div
      onClick={onOpen}
      className="flex cursor-pointer flex-col gap-3 rounded-[13px] border border-line-card bg-white px-5 py-[18px] transition-all hover:border-line-strong hover:shadow-[0_2px_10px_rgba(0,0,0,0.04)]"
    >
      <div className="flex items-start justify-between gap-4">
        <div className="flex flex-1 items-start gap-2.5">
          <button
            type="button"
            title="북마크"
            onClick={(e) => {
              e.stopPropagation();
              onToggleBookmark();
            }}
            className={`flex flex-shrink-0 items-center justify-center p-0.5 ${
              bookmarked ? "text-accent" : "text-icon"
            }`}
          >
            <svg width="17" height="17" viewBox="0 0 24 24" fill={bookmarked ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
            </svg>
          </button>
          <div className="flex-1 text-[15px] font-semibold leading-[1.5] text-ink">{note.q}</div>
        </div>
        <button
          type="button"
          title="오답노트에서 삭제"
          onClick={(e) => {
            e.stopPropagation();
            onDelete();
          }}
          className="flex flex-shrink-0 items-center justify-center rounded-[6px] p-1 text-axis transition-colors hover:bg-neutral hover:text-danger"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-[6px] bg-neutral px-[9px] py-[3px] text-xs font-medium text-secondary">
            {note.cat}
          </span>
          <span className="text-xs font-semibold" style={{ color: diffColor(note.diff) }}>
            난이도 {note.diff}
          </span>
        </div>
        <div className="flex items-center gap-2.5">
          <span className="text-xs font-medium text-placeholder">{note.solvedAt}</span>
          <Button
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              onResolve();
            }}
          >
            재풀이
          </Button>
        </div>
      </div>
    </div>
  );
}
