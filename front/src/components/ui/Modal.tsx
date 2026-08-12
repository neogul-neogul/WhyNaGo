"use client";

import { useEffect, useRef } from "react";

const FOCUSABLE =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

const DEFAULT_CARD_CLASS =
  "flex w-full max-w-110 flex-col items-center px-10 py-11 text-center";

export default function Modal({
  children,
  onClose,
  labelledBy,
  className = DEFAULT_CARD_CLASS,
}: {
  children: React.ReactNode;
  onClose?: () => void;
  labelledBy?: string;
  /** 카드 레이아웃(폭·패딩·정렬 등) 오버라이드. 기본값은 확인/안내형 모달 레이아웃 */
  className?: string;
}) {
  const cardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, []);

  useEffect(() => {
    cardRef.current?.querySelector<HTMLElement>(FOCUSABLE)?.focus();
  }, []);

  useEffect(() => {
    if (!onClose) return;
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [onClose]);

  const handleBackdropClick = (event: React.MouseEvent) => {
    if (onClose && event.target === event.currentTarget) onClose();
  };

  const trapTab = (event: React.KeyboardEvent) => {
    if (event.key !== "Tab") return;

    const items = Array.from(
      cardRef.current?.querySelectorAll<HTMLElement>(FOCUSABLE) ?? [],
    );
    if (items.length === 0) return;

    const first = items[0];
    const last = items[items.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  };

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center bg-ink/40 px-5 backdrop-blur-[2px]"
      onClick={handleBackdropClick}
    >
      <div
        ref={cardRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={labelledBy}
        onKeyDown={trapTab}
        className={`relative rounded-[18px] border border-line-card bg-white ${className}`}
      >
        {children}
      </div>
    </div>
  );
}
