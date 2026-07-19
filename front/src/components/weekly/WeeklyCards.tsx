import type { WeeklyCard } from "@/types";

// 주간 리포트 지표 카드 그리드 (라벨/값 좌측 + 증감 우측)
export default function WeeklyCards({ cards }: { cards: WeeklyCard[] }) {
  return (
    <div className="grid grid-cols-2 gap-[13px]">
      {cards.map((w) => (
        <div
          key={w.label}
          className="flex items-center justify-between gap-3 rounded-[13px] border border-line-card bg-white px-5 py-[18px]"
        >
          <div className="flex flex-col gap-[5px]">
            <span className="text-[12.5px] font-medium text-muted">{w.label}</span>
            <span className="text-[17px] font-bold">{w.value}</span>
          </div>
          <span className="font-mono text-[13px] font-semibold" style={{ color: w.deltaColor }}>
            {w.delta}
          </span>
        </div>
      ))}
    </div>
  );
}
