"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { AdminEmailBatch } from "@/types";
import { adminEmailBatches } from "@/mocks/admin";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";

const PAGE_SIZE = 4;

function StatusBadge({ status }: { status: AdminEmailBatch["status"] }) {
  return <Badge tone={status === "정상" ? "success" : "warning"}>{status}</Badge>;
}

export default function AdminEmailsPage() {
  const router = useRouter();
  const [page, setPage] = useState(0);

  const today = adminEmailBatches[0];
  const totalPages = Math.max(1, Math.ceil(adminEmailBatches.length / PAGE_SIZE));
  const current = Math.min(page, totalPages - 1);
  const rows = adminEmailBatches.slice(current * PAGE_SIZE, (current + 1) * PAGE_SIZE);

  const columns: AdminColumn<AdminEmailBatch>[] = [
    {
      key: "date",
      header: "날짜",
      width: 120,
      render: (b) => <span className="font-mono text-[13px] font-semibold text-ink">{b.date}</span>,
    },
    {
      key: "at",
      header: "실행시각",
      width: 120,
      render: (b) => <span className="font-mono text-[13px] font-medium text-secondary">{b.at}</span>,
    },
    {
      key: "status",
      header: "상태",
      width: 90,
      render: (b) => <StatusBadge status={b.status} />,
    },
    {
      key: "success",
      header: "성공",
      align: "right",
      render: (b) => (
        <span className="font-mono text-[13px] font-semibold text-success">
          {b.successCount.toLocaleString()}건
        </span>
      ),
    },
    {
      key: "fail",
      header: "실패",
      width: 90,
      align: "right",
      render: (b) => (
        <span
          className={`font-mono text-[13px] font-bold ${b.failCount > 0 ? "text-danger" : "text-placeholder"}`}
        >
          {b.failCount.toLocaleString()}건
        </span>
      ),
    },
    {
      key: "action",
      header: "액션",
      width: 76,
      align: "right",
      render: (b) => (
        <Button variant="secondary" size="sm" onClick={() => router.push(`/admin/emails/${b.date}`)}>
          상세
        </Button>
      ),
    },
  ];

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <Card className="flex flex-col gap-3 p-[22px]">
        <span className="text-[13px] font-semibold text-muted">오늘의 배치 요약</span>

        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2 xl:grid-cols-4">
          <SummaryTile label="발송 상태">
            <StatusBadge status={today.status} />
          </SummaryTile>
          <SummaryTile label="실행 시각">
            <span className="font-mono text-[13px] font-bold text-ink">
              {today.date} {today.at}
            </span>
          </SummaryTile>
          <SummaryTile label="대상자 수">
            <span className="font-mono text-[13px] font-bold text-ink">
              {today.targetCount.toLocaleString()}건
            </span>
          </SummaryTile>
          <SummaryTile label="성공/실패">
            <span className="font-mono text-[13px] font-bold">
              <span className="text-success">{today.successCount.toLocaleString()}</span>
              <span className="text-placeholder"> / </span>
              <span className="text-danger">{today.failCount.toLocaleString()}</span>
            </span>
          </SummaryTile>
        </div>

        {today.failureSummary && (
          <div className="flex flex-wrap items-center gap-3">
            <div className="min-w-[240px] flex-1 rounded-[10px] border border-alert-line bg-danger-bg px-3.5 py-2.5 text-[12.5px] text-danger">
              <strong className="font-bold">실패 사유 요약:</strong> {today.failureSummary}
            </div>
            <Button size="sm" onClick={() => router.push(`/admin/emails/${today.date}?failed=1`)}>
              실패자만 재시도
            </Button>
          </div>
        )}
      </Card>

      <AdminTable
        columns={columns}
        rows={rows}
        rowKey={(b) => b.date}
        onRowClick={(b) => router.push(`/admin/emails/${b.date}`)}
        emptyText="배치 실행 이력이 없습니다."
        caption={{ left: "배치 실행 이력" }}
        footer={
          <div className="flex justify-center border-t border-line-card px-[22px] py-4">
            <Pagination page={current} totalPages={totalPages} onChange={setPage} />
          </div>
        }
      />
    </div>
  );
}

function SummaryTile({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1 rounded-xl border border-line-card bg-subtle px-3.5 py-3">
      <span className="text-[11.5px] font-semibold text-soft">{label}</span>
      {children}
    </div>
  );
}
