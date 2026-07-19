import type { ProgressMetric } from "@/types";
import StatCard from "@/components/ui/StatCard";

// 진척도 지표 카드 그리드
export default function ProgressMetrics({ metrics }: { metrics: ProgressMetric[] }) {
  return (
    <div className="grid grid-cols-3 gap-[13px]">
      {metrics.map((m) => (
        <StatCard key={m.label} label={m.label}>
          <div className="flex items-baseline gap-[5px]">
            <span className="font-mono text-[26px] font-bold" style={{ color: m.color }}>
              {m.value}
            </span>
            <span className="text-[12.5px] text-placeholder">{m.unit}</span>
          </div>
        </StatCard>
      ))}
    </div>
  );
}
