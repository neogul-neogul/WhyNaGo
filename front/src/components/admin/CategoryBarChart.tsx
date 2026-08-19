import { palette } from "@/lib/tokens";
import { adminCategoryBars } from "@/mocks/admin";

// 막대 최대 높이 기준값 (시안 기준 8회)
const MAX_VALUE = 8;

// 출제 횟수 순서대로 진한 잉크 → 옅은 그레이
const BAR_COLORS = [
  palette.ink,
  palette.dim,
  palette.secondary,
  palette.placeholder,
  palette.muted,
  palette.icon,
  palette.lineStrong,
];

// 최근 30일 카테고리별 출제 횟수 (더미)
export default function CategoryBarChart() {
  return (
    <div className="grid h-[240px] grid-cols-7 items-end gap-[18px] px-2">
      {adminCategoryBars.map((bar, i) => (
        <div key={bar.label} className="flex h-full flex-col items-center justify-end gap-2">
          <span className="font-mono text-[13.5px] font-bold text-ink">{bar.value}</span>
          {/* 높이·색이 값에서 오므로 유틸리티 클래스로 표현할 수 없다 */}
          <div
            style={{
              height: `${Math.round((bar.value / MAX_VALUE) * 100)}%`,
              background: BAR_COLORS[i % BAR_COLORS.length],
            }}
            className="w-full rounded-t-[4px]"
          />
          <span className="text-xs font-semibold text-secondary">{bar.label}</span>
        </div>
      ))}
    </div>
  );
}
