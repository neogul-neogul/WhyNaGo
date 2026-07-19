import Link from "next/link";
import type { LearningMenuIcon, LearningMenuItem } from "@/types";

// 학습 메뉴 카드 아이콘 (더미 UI용 인라인 SVG)
function MenuCardIcon({ name, color }: { name: LearningMenuIcon; color: string }) {
  const common = {
    width: 21,
    height: 21,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: color,
    strokeWidth: 1.8,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };
  switch (name) {
    case "solve":
      return (
        <svg {...common}>
          <path d="M9 11l3 3L22 4" />
          <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
        </svg>
      );
    case "wrong":
      return (
        <svg {...common}>
          <path d="M4 4v16h16" />
          <path d="M8 16l3-4 2 2 4-6" />
        </svg>
      );
    case "interview":
      return (
        <svg {...common}>
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
        </svg>
      );
    case "mock":
      return (
        <svg {...common}>
          <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
        </svg>
      );
  }
}

// "오늘 완료 가능한 학습" 메뉴 카드 그리드
export default function LearningMenu({ items }: { items: LearningMenuItem[] }) {
  return (
    <div>
      <div className="mb-[13px] text-[13px] font-semibold text-muted">
        오늘 완료 가능한 학습
      </div>
      <div className="grid grid-cols-2 gap-3.5">
        {items.map((item) => (
          <Link
            key={item.key}
            href={item.href}
            className="flex items-center gap-4 rounded-[14px] border border-line-card bg-white px-[22px] py-5 text-left transition-colors hover:border-ink"
          >
            <div
              className="flex h-[42px] w-[42px] flex-shrink-0 items-center justify-center rounded-[11px]"
              style={{ backgroundColor: item.accentBg }}
            >
              <MenuCardIcon name={item.icon} color={item.accentFg} />
            </div>
            <div className="flex-1">
              <div className="mb-[3px] flex items-center gap-[7px]">
                <span className="text-[15px] font-semibold text-ink">
                  {item.title}
                </span>
                {item.badge && (
                  <span className="rounded-[5px] bg-ai-bg px-1.5 py-0.5 text-[10px] font-bold tracking-[0.03em] text-ai">
                    {item.badge}
                  </span>
                )}
              </div>
              <div className="text-[12.5px] text-soft">{item.description}</div>
            </div>
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              className="text-icon"
            >
              <path d="M9 18l6-6-6-6" />
            </svg>
          </Link>
        ))}
      </div>
    </div>
  );
}
