"use client";

import { useState } from "react";
import Link from "next/link";
import type { AdminInterviewRecord } from "@/types";
import { ADMIN_CATEGORIES } from "@/lib/admin";
import { adminInterviewAlerts, adminInterviews } from "@/mocks/admin";
import Card from "@/components/ui/Card";
import Chip from "@/components/ui/Chip";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import { CategoryBadge, DifficultyText } from "@/components/admin/AdminBadges";
import CategoryBarChart from "@/components/admin/CategoryBarChart";
import FilterSelect from "@/components/admin/FilterSelect";

const PAGE_SIZE = 7;

const PERIODS = ["최근 30일", "직접 지정"] as const;
type Period = (typeof PERIODS)[number];

// 기간별로 보여줄 이력 개수와 표기할 조회 구간 (더미)
const PERIOD_INFO: Record<Period, { count: number; range: string }> = {
  "최근 30일": { count: 28, range: "2026-07-16 ~ 2026-08-14" },
  "직접 지정": { count: 14, range: "2026-08-01 ~ 2026-08-14" },
};

const CATEGORY_OPTIONS = ["카테고리 전체", ...ADMIN_CATEGORIES];

const COLUMNS: AdminColumn<AdminInterviewRecord>[] = [
  {
    key: "date",
    header: "날짜",
    width: 56,
    render: (r) => <span className="font-mono text-[12.5px] font-medium text-secondary">{r.date}</span>,
  },
  {
    key: "body",
    header: "문제 본문",
    render: (r) => <span className="block truncate text-sm font-semibold text-ink">{r.body}</span>,
  },
  { key: "category", header: "카테고리", width: 140, render: (r) => <CategoryBadge category={r.category} /> },
  {
    key: "difficulty",
    header: "난이도",
    width: 74,
    render: (r) => <DifficultyText difficulty={r.difficulty} />,
  },
  {
    key: "participants",
    header: "참여자",
    width: 56,
    align: "right",
    render: (r) => (
      <span className="font-mono text-[13.5px] font-medium text-secondary">{r.participants}</span>
    ),
  },
  {
    key: "completionRate",
    header: "완료율",
    width: 60,
    align: "right",
    render: (r) => (
      <span className="font-mono text-[13.5px] font-semibold text-ink">{r.completionRate}</span>
    ),
  },
  {
    key: "within3MinRate",
    header: "3분 내 완료",
    width: 76,
    align: "right",
    render: (r) => (
      <span className="font-mono text-[13.5px] font-medium text-secondary">{r.within3MinRate}</span>
    ),
  },
  {
    key: "avgScore",
    header: "평균 점수",
    width: 66,
    align: "right",
    render: (r) => <span className="font-mono text-[13.5px] font-semibold text-ink">{r.avgScore}</span>,
  },
  {
    key: "action",
    header: "액션",
    width: 44,
    align: "right",
    render: (r) => (
      <Link
        href={`/admin/questions/${r.questionId}`}
        className="text-[13px] font-semibold text-accent transition-colors hover:text-ink"
      >
        상세
      </Link>
    ),
  },
];

export default function AdminInterviewsPage() {
  const [period, setPeriod] = useState<Period>("최근 30일");
  const [category, setCategory] = useState("카테고리 전체");
  const [page, setPage] = useState(0);

  const filtered = adminInterviews
    .slice(0, PERIOD_INFO[period].count)
    .filter((r) => category === "카테고리 전체" || r.category === category);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, totalPages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, (current + 1) * PAGE_SIZE);

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2.5">
          {PERIODS.map((p) => (
            <Chip
              key={p}
              label={p}
              active={period === p}
              onClick={() => {
                setPeriod(p);
                setPage(0);
              }}
            />
          ))}
          <FilterSelect
            variant="chip"
            value={category}
            options={CATEGORY_OPTIONS}
            onChange={(v) => {
              setCategory(v);
              setPage(0);
            }}
          />
        </div>
        <span className="font-mono text-[12.5px] font-medium text-placeholder">
          {PERIOD_INFO[period].range}
        </span>
      </div>

      <div className="grid grid-cols-1 gap-3.5 lg:grid-cols-2">
        {adminInterviewAlerts.map((alert) => (
          // 카드 배경/테두리가 경고 색이라 공통 Card 대신 직접 그린다
          <div
            key={alert.title}
            className={`flex items-center gap-3 rounded-[16px] border px-[22px] py-[18px] ${
              alert.tone === "warning"
                ? "border-warning/25 bg-warning-bg"
                : "border-alert-line bg-danger-bg"
            }`}
          >
            <span
              className={`h-[22px] w-[22px] flex-shrink-0 rounded-full ${
                alert.tone === "warning" ? "bg-warning" : "bg-danger"
              }`}
            />
            <span className="flex flex-col gap-1">
              <span
                className={`text-sm font-bold ${alert.tone === "warning" ? "text-alert" : "text-danger"}`}
              >
                {alert.title}
              </span>
              <span
                className={`text-[12.5px] font-medium text-alert-deep ${alert.mono ? "font-mono" : ""}`}
              >
                {alert.detail}
              </span>
            </span>
          </div>
        ))}
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={rows}
        rowKey={(r) => r.date}
        emptyText="조건에 맞는 출제 이력이 없습니다."
      />

      <Card className="flex flex-col gap-[18px] p-6">
        <span className="text-[13px] font-semibold text-secondary">
          최근 30일 카테고리별 출제 횟수
        </span>
        <CategoryBarChart />
      </Card>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <span className="font-mono text-[12.5px] font-medium text-placeholder">
          총 {filtered.length}건 ·{" "}
          {rows.length ? `${current * PAGE_SIZE + 1}-${current * PAGE_SIZE + rows.length}` : "0"} 표시
        </span>
        <Pagination page={current} totalPages={totalPages} onChange={setPage} />
      </div>
    </div>
  );
}
