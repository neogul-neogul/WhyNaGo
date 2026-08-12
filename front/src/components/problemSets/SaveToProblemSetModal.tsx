"use client";

import { useState } from "react";
import type { ProblemSetMembershipResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { addProblemSetItem, createProblemSet, removeProblemSetItem } from "@/lib/problemSets";
import Modal from "@/components/ui/Modal";

// 문제 풀이 화면에서 "문제집에 저장" 클릭 시 뜨는 모달 — 유튜브 재생목록 담기와 같은 흐름
export default function SaveToProblemSetModal({
  open,
  onClose,
  questionId,
  questionTitle,
  membership,
  onMembershipChange,
}: {
  open: boolean;
  onClose: () => void;
  questionId: number;
  questionTitle: string;
  membership: ProblemSetMembershipResponse[];
  onMembershipChange: (membership: ProblemSetMembershipResponse[]) => void;
}) {
  if (!open) return null;
  return (
    <SaveToProblemSetModalContent
      onClose={onClose}
      questionId={questionId}
      questionTitle={questionTitle}
      membership={membership}
      onMembershipChange={onMembershipChange}
    />
  );
}

function SaveToProblemSetModalContent({
  onClose,
  questionId,
  questionTitle,
  membership,
  onMembershipChange,
}: {
  onClose: () => void;
  questionId: number;
  questionTitle: string;
  membership: ProblemSetMembershipResponse[];
  onMembershipChange: (membership: ProblemSetMembershipResponse[]) => void;
}) {
  const [pendingId, setPendingId] = useState<number | null>(null);
  const [newOpen, setNewOpen] = useState(false);
  const [newName, setNewName] = useState("");
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canCreate = newName.trim().length > 0 && !creating;

  const toggle = async (set: ProblemSetMembershipResponse) => {
    if (pendingId !== null) return;
    setPendingId(set.id);
    setError(null);
    try {
      if (set.saved) {
        await removeProblemSetItem(set.id, questionId);
      } else {
        await addProblemSetItem(set.id, questionId);
      }
      onMembershipChange(
        membership.map((m) =>
          m.id === set.id
            ? { ...m, saved: !m.saved, itemCount: m.itemCount + (m.saved ? -1 : 1) }
            : m,
        ),
      );
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "문제집을 변경하지 못했습니다.");
    } finally {
      setPendingId(null);
    }
  };

  const create = async () => {
    if (!canCreate) return;
    setCreating(true);
    setError(null);
    try {
      const created = await createProblemSet({ name: newName });
      onMembershipChange([
        { id: created.id, name: created.name, itemCount: 0, saved: false },
        ...membership,
      ]);
      setNewOpen(false);
      setNewName("");
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "문제집을 만들지 못했습니다.");
    } finally {
      setCreating(false);
    }
  };

  return (
    <Modal
      labelledBy="save-to-problem-set-title"
      onClose={onClose}
      className="flex w-full max-w-[400px] flex-col overflow-hidden text-left shadow-[0_24px_60px_rgba(0,0,0,.24)]"
    >
      <div className="flex flex-col gap-1 border-b border-line-soft px-[22px] pb-[15px] pt-5">
        <span id="save-to-problem-set-title" className="text-[16px] font-bold tracking-[-0.2px] text-ink">
          문제집에 저장
        </span>
        <span className="truncate text-[12.5px] text-soft">{questionTitle}</span>
      </div>

      <div className="flex max-h-[264px] flex-col gap-0.5 overflow-y-auto p-2">
        {membership.length === 0 && (
          <div className="px-3 py-6 text-center text-[13px] text-soft">
            아직 만든 문제집이 없습니다
          </div>
        )}
        {membership.map((set) => (
          <button
            key={set.id}
            type="button"
            disabled={pendingId !== null}
            onClick={() => toggle(set)}
            className="flex w-full items-center gap-3 rounded-[10px] px-3 py-2.5 text-left transition-colors hover:bg-subtle disabled:cursor-not-allowed"
          >
            <span className="flex min-w-0 flex-1 flex-col gap-0.5">
              <span className="truncate text-[14px] font-semibold text-ink">{set.name}</span>
              <span className="text-[11.5px] text-placeholder">{set.itemCount}문제</span>
            </span>
            <span
              className={`flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-[6px] border-[1.5px] ${
                set.saved ? "border-ink bg-ink" : "border-[#D4D4CC] bg-white"
              }`}
            >
              {set.saved && <span className="text-[12px] font-extrabold text-white">✓</span>}
            </span>
          </button>
        ))}
      </div>

      {error && <div className="px-3.5 pt-2 text-[12.5px] text-danger">{error}</div>}

      <div className="flex flex-col gap-[11px] border-t border-line-soft px-3.5 pb-3.5 pt-3">
        {!newOpen && (
          <button
            type="button"
            onClick={() => setNewOpen(true)}
            className="flex items-center gap-2 rounded-[9px] px-2.5 py-2 text-[13.5px] font-semibold text-accent transition-colors hover:bg-accent-faint"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round">
              <path d="M12 5v14M5 12h14" />
            </svg>
            새 문제집 만들기
          </button>
        )}

        {newOpen && (
          <div className="flex flex-col gap-2.5 rounded-[12px] border border-line-card bg-subtle p-3.5">
            <input
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              placeholder="문제집 이름"
              className="w-full rounded-[10px] border border-line-input bg-white px-[13px] py-[11px] text-[13.5px] text-ink outline-none placeholder:text-placeholder"
            />
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  setNewOpen(false);
                  setNewName("");
                }}
                className="rounded-[9px] px-3.5 py-2 text-[13px] font-semibold text-secondary transition-colors hover:bg-white"
              >
                취소
              </button>
              <button
                type="button"
                onClick={create}
                disabled={!canCreate}
                className={`rounded-[9px] px-4 py-2 text-[13px] font-semibold text-white transition-colors ${
                  canCreate ? "cursor-pointer bg-ink hover:bg-ink-hover" : "cursor-not-allowed bg-icon"
                }`}
              >
                만들기
              </button>
            </div>
          </div>
        )}

        <button
          type="button"
          onClick={onClose}
          className="w-full rounded-[10px] bg-ink py-3 text-[14px] font-semibold text-white transition-colors hover:bg-ink-hover"
        >
          완료
        </button>
      </div>
    </Modal>
  );
}
