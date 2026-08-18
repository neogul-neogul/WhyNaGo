import Link from "next/link";
import { adminKpis, adminTodayInterview } from "@/mocks/admin";
import Badge from "@/components/ui/Badge";
import Card, { CardHeader } from "@/components/ui/Card";
import StatCard from "@/components/ui/StatCard";
import { CategoryBadge, DifficultyBadge } from "@/components/admin/AdminBadges";
import SolveTrendChart from "@/components/admin/SolveTrendChart";

const QUICK_MENU = [
  { label: "문제 목록", href: "/admin/questions", badge: null },
  { label: "회원 검색", href: "/admin/members", badge: null },
  { label: "메일 실패 건 확인", href: "/admin/emails?filter=failed", badge: "12" },
] as const;

export default function AdminDashboardPage() {
  const summary = adminTodayInterview;

  return (
    <div className="flex w-full flex-col gap-5">
      <div className="grid grid-cols-4 gap-5">
        {adminKpis.map((kpi) => (
          <StatCard key={kpi.label} label={kpi.label}>
            <span className="font-mono text-[26px] font-bold tracking-[-0.5px] text-ink">
              {kpi.value}
              <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">
                {kpi.unit}
              </span>
            </span>
            <span
              className={`text-[12.5px] font-semibold ${kpi.increased ? "text-success" : "text-danger"}`}
            >
              {kpi.delta}
            </span>
          </StatCard>
        ))}
      </div>

      <div className="grid grid-cols-[1.6fr_1fr] items-start gap-[18px]">
        <Card className="overflow-hidden">
          <CardHeader className="justify-between gap-3">
            <span className="text-[13px] font-semibold text-secondary">
              오늘의 1일1면접 요약 · {summary.date}
            </span>
            <Link
              href="/admin/interviews"
              className="text-[13px] font-semibold text-secondary transition-colors hover:text-ink"
            >
              상세 보기 →
            </Link>
          </CardHeader>

          <div className="flex flex-col gap-3.5 px-[22px] py-5">
            <div className="flex items-center gap-2">
              <CategoryBadge category={summary.category} />
              <DifficultyBadge difficulty={summary.difficulty} />
            </div>
            <p className="text-pretty text-[15.5px] font-semibold leading-[1.6] text-ink">
              {summary.body}
            </p>
            <div className="flex items-end gap-8 border-t border-dashed border-line pt-3.5">
              <SummaryMetric label="참여자 수" value={summary.participants} unit="명" />
              <SummaryMetric label="완주율" value={summary.completionRate} />
              <SummaryMetric label="평균점수" value={summary.avgScore} />
            </div>
          </div>
        </Card>

        <Card className="overflow-hidden">
          <CardHeader>
            <span className="text-[13px] font-semibold text-secondary">빠른 메뉴</span>
          </CardHeader>
          <div className="flex flex-col gap-2.5 px-5 py-[18px]">
            {QUICK_MENU.map((item) => (
              <Link
                key={item.label}
                href={item.href}
                className="flex items-center justify-between gap-3 rounded-[11px] border border-line-input bg-white px-[18px] py-4 text-sm font-semibold text-ink transition-colors hover:border-ink"
              >
                <span className="flex items-center gap-2.5">
                  {item.label}
                  {item.badge && (
                    <Badge tone="danger" className="font-mono">
                      {item.badge}
                    </Badge>
                  )}
                </span>
                <span className="text-placeholder">→</span>
              </Link>
            ))}
          </div>
        </Card>
      </div>

      <Card className="flex flex-col gap-[18px] p-6">
        <span className="text-[13px] font-semibold text-secondary">
          일별 풀이 수 추이 · 최근 30일
        </span>
        <SolveTrendChart />
      </Card>
    </div>
  );
}

function SummaryMetric({ label, value, unit }: { label: string; value: string; unit?: string }) {
  return (
    <div className="flex flex-col gap-[5px]">
      <span className="text-xs font-semibold text-placeholder">{label}</span>
      <span className="font-mono text-xl font-bold text-ink">
        {value}
        {unit && <span className="font-sans text-[13px] text-placeholder">{unit}</span>}
      </span>
    </div>
);
}
