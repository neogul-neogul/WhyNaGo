"use client";

import { useEffect, useState } from "react";
import type { ProblemSetSummaryResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { createProblemSet, fetchProblemSets, formatProblemSetDate } from "@/lib/problemSets";

function PlaylistIcon({ className }: { className?: string }) {
  return (
    <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M3 6h11M3 12h11M3 18h7" />
      <path d="M17 11l5 3-5 3z" />
    </svg>
  );
}

// 나의 문제집 목록 (카드 그리드) — "문제집" 탭 — GET/POST /api/problem-sets
export default function ProblemSetList({ onOpen }: { onOpen: (id: number) => void }) {
  const [problemSets, setProblemSets] = useState<ProblemSetSummaryResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [newOpen, setNewOpen] = useState(false);
  const [newName, setNewName] = useState("");
  const [creating, setCreating] = useState(false);

  const load = () => {
    fetchProblemSets()
      .then((list) => {
        setProblemSets(list);
        setError(null);
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : "문제집 목록을 불러오지 못했습니다."));
  };

  useEffect(load, []);

  const canCreate = newName.trim().length > 0 && !creating;

  const create = async () => {
    if (!canCreate) return;
    setCreating(true);
    try {
      await createProblemSet({ name: newName });
      setNewOpen(false);
      setNewName("");
      load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "문제집을 만들지 못했습니다.");
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="flex max-w-[1000px] flex-col gap-4">
      <div className="flex min-h-10 items-center justify-between gap-3">
        <span className="text-[14px] font-semibold text-ink">
          <span className="font-mono">{problemSets?.length ?? 0}</span>개 문제집
        </span>
        {!newOpen && (
          <button
            type="button"
            onClick={() => setNewOpen(true)}
            className="flex items-center gap-[7px] rounded-[10px] bg-ink px-[18px] py-2.5 text-[13.5px] font-semibold text-white transition-colors hover:bg-ink-hover"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round">
              <path d="M12 5v14M5 12h14" />
            </svg>
            새 문제집 만들기
          </button>
        )}
      </div>

      {newOpen && (
        <div className="flex flex-col gap-[13px] rounded-[14px] border border-line-card bg-white px-[22px] py-5">
          <span className="text-[14px] font-bold text-ink">새 문제집 만들기</span>
          <input
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            placeholder="예) 면접 D-7 벼락치기"
            className="w-full rounded-[11px] border border-line-input bg-subtle px-[15px] py-3 text-[14px] text-ink outline-none placeholder:text-placeholder"
          />
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => {
                setNewOpen(false);
                setNewName("");
              }}
              className="rounded-[9px] border border-line-strong bg-white px-[18px] py-2.5 text-[13.5px] font-semibold text-secondary transition-colors hover:border-ink"
            >
              취소
            </button>
            <button
              type="button"
              onClick={create}
              disabled={!canCreate}
              className={`rounded-[9px] px-[18px] py-2.5 text-[13.5px] font-semibold text-white transition-colors ${
                canCreate ? "cursor-pointer bg-ink hover:bg-ink-hover" : "cursor-not-allowed bg-icon"
              }`}
            >
              만들기
            </button>
          </div>
        </div>
      )}

      {error && (
        <div className="rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-danger">
          {error}
        </div>
      )}

      {!error && problemSets === null && (
        <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">문제집을 불러오는 중…</div>
      )}

      {!error && problemSets !== null && (
        <div className="grid grid-cols-2 gap-[14px]">
          {problemSets.map((set) => (
            <button
              key={set.id}
              type="button"
              onClick={() => onOpen(set.id)}
              className="flex flex-col gap-3 rounded-[14px] border border-line-card bg-white px-[22px] py-5 text-left transition-colors hover:border-ink"
            >
              <div className="flex w-full items-start gap-3">
                <div className="flex h-[38px] w-[38px] flex-shrink-0 items-center justify-center rounded-[10px] bg-accent-bg text-accent">
                  <PlaylistIcon />
                </div>
                <div className="flex min-w-0 flex-1 flex-col gap-0.5">
                  <span className="truncate text-[15px] font-bold text-ink">{set.name}</span>
                  <span className="text-[12.5px] text-soft">
                    {set.itemCount}문제 · 수정 {formatProblemSetDate(set.updatedAt)}
                  </span>
                </div>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" className="flex-shrink-0 text-icon">
                  <path d="M9 18l6-6-6-6" />
                </svg>
              </div>
              <div className="flex w-full flex-col gap-[5px] border-t border-line-soft pt-[11px]">
                {set.previewTitles.map((title) => (
                  <span key={title} className="truncate text-[12.5px] text-secondary">
                    {title}
                  </span>
                ))}
                {set.previewTitles.length === 0 && (
                  <span className="text-[12.5px] text-placeholder">아직 담은 문제가 없습니다</span>
                )}
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
