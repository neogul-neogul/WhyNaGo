import Link from "next/link";

/** 홈에서 맞춤 문제 추천 화면으로 들어가는 카드 */
export default function RecommendationEntry({ weakTag }: { weakTag: string | null }) {
  return (
    <Link
      href="/recommend"
      className="flex items-center gap-[18px] rounded-[16px] border border-line-card bg-white px-6 py-5 text-left transition-colors hover:border-ink"
    >
      <div className="flex h-[46px] w-[46px] flex-shrink-0 items-center justify-center rounded-[12px] bg-accent-bg">
        <SparkleIcon className="text-accent" />
      </div>
      <div className="flex-1">
        <div className="mb-1 flex items-center gap-2">
          <span className="text-[15.5px] font-semibold text-ink">맞춤 문제 추천</span>
          {weakTag && (
            <span className="rounded-[5px] bg-alert-tint px-[7px] py-0.5 text-[10.5px] font-bold text-alert">
              취약 #{weakTag}
            </span>
          )}
        </div>
        <p className="text-[12.5px] text-soft">
          내 취약점을 확인하고, 그 영역에서 한 문제를 생성해 바로 풀어보세요
        </p>
      </div>
      <span className="text-[13.5px] font-semibold text-ink">추천 보기</span>
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" className="text-icon">
        <path d="M9 18l6-6-6-6" />
      </svg>
    </Link>
  );
}

export function SparkleIcon({ className = "" }: { className?: string }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M12 3l2.2 5.3 5.8.5-4.4 3.8 1.3 5.6L12 15.4 7.1 18.2l1.3-5.6L4 8.8l5.8-.5z" />
    </svg>
  );
}
