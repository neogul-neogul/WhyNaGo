"use client";

import Modal from "@/components/ui/Modal";

// 되돌릴 수 없는 동작(삭제 등) 전에 확인을 받는 공용 다이얼로그
export default function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = "삭제",
  cancelLabel = "취소",
  onConfirm,
  onCancel,
}: {
  open: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  if (!open) return null;

  return (
    <Modal labelledBy="confirm-dialog-title" onClose={onCancel}>
      <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-danger-bg text-danger">
        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round">
          <path d="M4 7h16M9 7V5a2 2 0 012-2h2a2 2 0 012 2v2m3 0-1 13a2 2 0 01-2 2H8a2 2 0 01-2-2L5 7h14z" />
          <path d="M10 11v6M14 11v6" />
        </svg>
      </div>

      <h2 id="confirm-dialog-title" className="mb-2 text-lg font-semibold text-ink">
        {title}
      </h2>
      {description && <p className="mb-6 text-[13.5px] text-soft">{description}</p>}

      <div className="flex w-full gap-2">
        <button
          type="button"
          onClick={onCancel}
          className="flex-1 rounded-[11px] border border-line-strong bg-white px-5 py-2.5 text-sm font-semibold text-ink transition-colors hover:border-ink"
        >
          {cancelLabel}
        </button>
        <button
          type="button"
          onClick={onConfirm}
          className="flex-1 rounded-[11px] bg-danger px-5 py-2.5 text-sm font-semibold text-white transition-opacity hover:opacity-90"
        >
          {confirmLabel}
        </button>
      </div>

      <button
        type="button"
        onClick={onCancel}
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
