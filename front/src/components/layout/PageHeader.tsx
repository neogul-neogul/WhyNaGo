import TodayDate from "@/components/ui/TodayDate";

// 각 페이지 상단 헤더 (제목 + 부제 + 오늘 날짜) — 디자인 시안 기준
export default function PageHeader({
  title,
  subtitle,
}: {
  title: string;
  subtitle: string;
}) {
  return (
    <div className="border-b border-line bg-page/85 py-[18px]">
      <div className="mx-auto flex w-full max-w-[1180px] items-end justify-between px-9">
        <div className="flex flex-col gap-[3px]">
          <h1 className="text-[21px] font-bold tracking-[-0.4px] text-ink">
            {title}
          </h1>
          <p className="text-[13px] text-muted">{subtitle}</p>
        </div>
        <div className="flex items-center gap-1.5 rounded-[9px] border border-line-card bg-white px-3 py-[7px] text-[12.5px] font-medium text-secondary">
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
          >
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <path d="M16 2v4M8 2v4M3 10h18" />
          </svg>
          <TodayDate />
        </div>
      </div>
    </div>
  );
}

// 페이지 본문 래퍼 (최대폭 + 좌우 패딩) — 디자인 시안 기준
export function PageBody({ children }: { children: React.ReactNode }) {
  return (
    <div className="mx-auto w-full max-w-[1180px] flex-1 self-center px-9 pb-[60px] pt-8">
      {children}
    </div>
  );
}
