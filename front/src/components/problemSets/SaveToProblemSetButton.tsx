// 문제 풀이 화면 상단의 "문제집에 저장" 토글 버튼
export default function SaveToProblemSetButton({
  saved,
  onClick,
}: {
  saved: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex items-center gap-[7px] rounded-[9px] border px-3.5 py-2 text-[13px] font-semibold transition-all ${
        saved ? "border-ink bg-ink text-white" : "border-line-strong bg-white text-body"
      }`}
    >
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M3 6h11M3 12h11M3 18h7" />
        <path d="M17 11l5 3-5 3z" />
      </svg>
      {saved ? "문제집에 저장됨" : "문제집에 저장"}
    </button>
  );
}
