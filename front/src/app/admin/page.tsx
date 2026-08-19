"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import type { DashboardResponse } from "@/types";
import { ApiError } from "@/lib/api";
import {
  DASHBOARD_KPI_PLACEHOLDERS,
  fetchAdminDashboard,
  toDashboardAlerts,
  toDashboardKpis,
} from "@/lib/admin";
import { adminAiUsage } from "@/mocks/admin";
import StatCard from "@/components/ui/StatCard";

export default function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchAdminDashboard()
      .then((response) => {
        if (!cancelled) setDashboard(response);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "대시보드를 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  // 조회 전에는 라벨만 있는 자리 표시 카드를 그려 레이아웃이 흔들리지 않게 한다
  const kpis = dashboard ? toDashboardKpis(dashboard) : DASHBOARD_KPI_PLACEHOLDERS;
  const alerts = dashboard ? toDashboardAlerts(dashboard.alerts) : [];

  return (
    <div className="flex w-full flex-col gap-[26px]">
      {error && (
        <div className="rounded-2xl border border-alert-line border-l-[3px] border-l-danger bg-white px-5 py-4 text-[12.5px] font-medium text-secondary">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {kpis.map((kpi) => (
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

      {alerts.length > 0 && (
        <div className="flex flex-col gap-2.5">
          <span className="text-[13px] font-bold text-muted">알림</span>
          <div className="flex flex-col gap-2.5">
            {alerts.map((alert) => (
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
      )}

      {/* AI 사용량은 호출 로깅이 없어 백엔드가 내려주지 않는다. 목업 값이다. */}
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
