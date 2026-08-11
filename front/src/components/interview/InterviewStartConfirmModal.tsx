"use client";

import Modal from "@/components/ui/Modal";

// 면접 시작 직전 최종 확인 — 하루 1회 제한과 이탈 시 재도전 불가를 다시 한번 알린다
export default function InterviewStartConfirmModal({
  onConfirm,
  onClose,
}: {
  onConfirm: () => void;
  onClose: () => void;
}) {
  return (
    <Modal labelledBy="interview-start-confirm-title" onClose={onClose}>
      <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-alert-tint text-alert">
        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round">
          <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
          <path d="M12 9v4M12 17h.01" />
        </svg>
      </div>

      <h2 id="interview-start-confirm-title" className="mb-2 text-lg font-semibold text-ink">
        시작 전에 확인해주세요
      </h2>
      <p className="mb-6 flex flex-col gap-1.5 text-[13.5px] leading-[1.6] text-soft">
        <span>면접은 <b className="text-ink">하루에 한 번</b>만 볼 수 있어요.</span>
        <span>중간에 창을 닫거나 나가면 <b className="text-ink">오늘 다시 볼 수 없어요.</b></span>
      </p>

      <div className="flex w-full gap-2">
        <button
          type="button"
          onClick={onClose}
          className="flex-1 rounded-[11px] border border-line-strong bg-white px-5 py-2.5 text-sm font-semibold text-ink transition-colors hover:border-ink"
        >
          취소
        </button>
        <button
          type="button"
          onClick={onConfirm}
          className="flex-1 rounded-[11px] bg-ink px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-ink-hover"
        >
          시작하기
        </button>
      </div>

      <button
        type="button"
        onClick={onClose}
        aria-label="닫기"
        className="absolute right-4 top-4 text-soft transition-colors hover:text-ink"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </Modal>
  );
}
