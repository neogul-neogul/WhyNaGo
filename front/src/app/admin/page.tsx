import Link from "next/link";
import { adminAiUsage, adminDashboardAlerts, adminKpis } from "@/mocks/admin";
import StatCard from "@/components/ui/StatCard";

export default function AdminDashboardPage() {
  return (
    <div className="flex w-full flex-col gap-[26px]">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {adminKpis.map((kpi) => (
          <StatCard key={kpi.label} label={kpi.label}>
            <span className="font-mono text-2xl font-bold tracking-[-0.5px] text-ink">
              {kpi.value}
              {kpi.unit && (
                <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">
                  {kpi.unit}
                </span>
              )}
            </span>
            {kpi.delta && (
              <span
                className={`text-xs font-semibold ${kpi.increased ? "text-success" : "text-danger"}`}
              >
                {kpi.delta}
              </span>
            )}
            {kpi.breakdown && (
              <span className="font-mono text-xs font-medium text-placeholder">
                {kpi.breakdown}
              </span>
            )}
          </StatCard>
        ))}
      </div>

      <div className="flex flex-col gap-2.5">
        <span className="text-[13px] font-bold text-muted">알림</span>
        <div className="flex flex-col gap-2.5">
          {adminDashboardAlerts.map((alert) => (
            <div
              key={alert.title}
              className="flex flex-wrap items-center justify-between gap-[18px] rounded-2xl border border-alert-line border-l-[3px] border-l-danger bg-white px-5 py-4"
            >
              <div className="flex min-w-0 flex-col gap-[5px]">
                <span className="text-sm font-bold text-ink">{alert.title}</span>
                <span className="text-[12.5px] font-medium leading-[1.6] text-secondary">
                  {alert.detail}
                </span>
              </div>
              <Link
                href={alert.ctaHref}
                className="flex-none whitespace-nowrap rounded-[9px] bg-ink px-4 py-[9px] text-[12.5px] font-semibold text-white transition-colors hover:bg-ink-hover"
              >
                {alert.ctaLabel}
              </Link>
            </div>
          ))}
        </div>
      </div>

      <div className="flex flex-col gap-2.5">
        <span className="text-[13px] font-bold text-muted">AI 사용량</span>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          {adminAiUsage.map((kpi) => (
            <StatCard key={kpi.label} label={kpi.label}>
              <span className="font-mono text-2xl font-bold tracking-[-0.5px] text-ink">
                {kpi.value}
                {kpi.unit && (
                  <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">
                    {kpi.unit}
                  </span>
                )}
              </span>
            </StatCard>
          ))}
        </div>
      </div>
  </div>
  );
}
