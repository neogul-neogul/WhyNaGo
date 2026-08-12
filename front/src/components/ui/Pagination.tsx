"use client";

const WINDOW_SIZE = 5;

function pageWindow(page: number, totalPages: number): number[] {
  const count = Math.min(WINDOW_SIZE, totalPages);
  const start = Math.max(0, Math.min(page - Math.floor(WINDOW_SIZE / 2), totalPages - count));
  return Array.from({ length: count }, (_, i) => start + i);
}

export default function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}) {
  if (totalPages <= 1) return null;

  const pages = pageWindow(page, totalPages);

  return (
    <nav aria-label="페이지 이동" className="flex items-center justify-center gap-1.5 pt-2">
      <ArrowButton
        label="이전 페이지"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
        direction="prev"
      />
      {pages[0] > 0 && (
        <>
          <PageButton page={0} active={false} onClick={onChange} />
          {pages[0] > 1 && <Ellipsis />}
        </>
      )}
      {pages.map((p) => (
        <PageButton key={p} page={p} active={p === page} onClick={onChange} />
      ))}
      {pages[pages.length - 1] < totalPages - 1 && (
        <>
          {pages[pages.length - 1] < totalPages - 2 && <Ellipsis />}
          <PageButton page={totalPages - 1} active={false} onClick={onChange} />
        </>
      )}
      <ArrowButton
        label="다음 페이지"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
        direction="next"
      />
    </nav>
  );
}

function PageButton({
  page,
  active,
  onClick,
}: {
  page: number;
  active: boolean;
  onClick: (page: number) => void;
}) {
  return (
    <button
      type="button"
      aria-current={active ? "page" : undefined}
      onClick={() => onClick(page)}
      className={`h-8 min-w-8 cursor-pointer rounded-[8px] border px-2 font-mono text-[13px] transition-colors ${
        active
          ? "border-ink bg-ink font-semibold text-white"
          : "border-line-input bg-white font-medium text-dim hover:bg-subtle"
      }`}
    >
      {page + 1}
    </button>
  );
}

function ArrowButton({
  label,
  disabled,
  onClick,
  direction,
}: {
  label: string;
  disabled: boolean;
  onClick: () => void;
  direction: "prev" | "next";
}) {
  return (
    <button
      type="button"
      aria-label={label}
      disabled={disabled}
      onClick={onClick}
      className="flex h-8 w-8 items-center justify-center rounded-[8px] border border-line-input bg-white text-dim transition-colors hover:bg-subtle disabled:cursor-default disabled:opacity-40 disabled:hover:bg-white"
    >
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d={direction === "prev" ? "M15 6l-6 6 6 6" : "M9 6l6 6-6 6"} />
      </svg>
    </button>
  );
}

function Ellipsis() {
  return (
    <span aria-hidden className="px-0.5 text-[13px] text-placeholder">
      …
    </span>
  );
}
