"use client";

import Modal from "@/components/ui/Modal";
import Button from "@/components/ui/Button";

// 면접 중 화면을 벗어났다 돌아왔을 때 뜨는 경고.
// 타이머는 이탈 중에도 계속 흐른다 — 실제 면접과 같은 조건을 유지하기 위해서다.
export default function FocusWarningDialog({
  count,
  onClose,
}: {
  count: number;
  onClose: () => void;
}) {
  return (
    <Modal labelledBy="focus-warning-title" onClose={onClose}>
      <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-alert-bg text-alert">
        <svg
          width="26"
          height="26"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.9"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M12 9v4M12 17h.01" />
          <path d="M10.3 3.9L1.8 18a2 2 0 001.7 3h17a2 2 0 001.7-3L13.7 3.9a2 2 0 00-3.4 0z" />
        </svg>
      </div>

      <h2 id="focus-warning-title" className="mb-2 text-lg font-semibold text-ink">
        면접 중 화면을 벗어났습니다
      </h2>
      <p className="mb-1.5 text-[13.5px] text-soft">
        지금까지 <b className="text-alert-deep">{count}회</b> 감지되었어요. 이탈 횟수는 결과에 함께
        기록됩니다.
      </p>
      <p className="mb-6 text-[12.5px] text-soft">
        타이머는 이탈 중에도 계속 흐릅니다.
      </p>

      <Button size="lg" onClick={onClose} className="w-full">
        면접 이어하기
      </Button>
    </Modal>
  );
}
