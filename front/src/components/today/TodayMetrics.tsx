import type { MetricTone, TodayMetric } from "@/types";
import StatCard from "@/components/ui/StatCard";

// 지표 카드 값 색상
const metricValueColor: Record<MetricTone, string> = {
  default: "text-ink",
  warning: "text-alert",
  success: "text-success",
};

// 오늘 지표 카드 그리드 (푼 문제 / 면접 / 오답 복습)
export default function TodayMetrics({ metrics }: { metrics: TodayMetric[] }) {
  return (
    <div className="grid grid-cols-3 gap-3.5">
      {metrics.map((m) => (
        <StatCard key={m.key} label={m.label}>
          {m.unit ? (
            <div className="flex items-baseline gap-1.5">
              <span className="font-mono text-[28px] font-bold text-ink">
                {m.value}
              </span>
              <span className="text-[13px] text-placeholder">{m.unit}</span>
            </div>
          ) : (
            <div className="flex items-center gap-[7px]">
              <span className={`text-xl font-bold ${metricValueColor[m.tone]}`}>
                {m.value}
              </span>
              {m.note && (
                <span className="text-[13px] text-placeholder">{m.note}</span>
              )}
            </div>
          )}
        </StatCard>
      ))}
    </div>
  );
}
