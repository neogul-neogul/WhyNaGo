"use client";

import { Suspense, useState } from "react";
import { useSearchParams } from "next/navigation";
import type { AdminEmailLog } from "@/types";
import { adminEmailLogs } from "@/mocks/admin";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Chip from "@/components/ui/Chip";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";

const PAGE_SIZE = 8;

const FILTERS = [
  { key: "all", label: "전체 발송 이력" },
  { key: "failed", label: "실패 건만" },
] as const;

type FilterKey = (typeof FILTERS)[number]["key"];

export default function AdminEmailsPage() {
  // useSearchParams는 Suspense 경계 안에서만 쓸 수 있다 (대시보드 빠른 메뉴가 ?filter=failed로 들어온다)
  return (
    <Suspense fallback={null}>
      <EmailLogs />
    </Suspense>
  );
}

function EmailLogs() {
  const searchParams = useSearchParams();
  const [filter, setFilter] = useState<FilterKey>(
    searchParams.get("filter") === "failed" ? "failed" : "all",
  );
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<string[]>([]);
  const [resent, setResent] = useState<string[]>([]);

  const filtered = adminEmailLogs.filter((e) => filter === "all" || !e.succeeded);
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, totalPages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, (current + 1) * PAGE_SIZE);

  const isResent = (log: AdminEmailLog) => resent.includes(log.key);
  const isFailed = (log: AdminEmailLog) => !log.succeeded && !isResent(log);

  const toggle = (log: AdminEmailLog) => {
    if (!isFailed(log)) return;
    setSelected((prev) =>
      prev.includes(log.key) ? prev.filter((k) => k !== log.key) : [...prev, log.key],
    );
  };

  const resend = (keys: string[]) => {
    setResent((prev) => [...prev, ...keys]);
    setSelected((prev) => prev.filter((k) => !keys.includes(k)));
  };

  const failedCount = adminEmailLogs.filter((e) => !e.succeeded && !resent.includes(e.key)).length;

  const columns: AdminColumn<AdminEmailLog>[] = [
    {
      key: "check",
      header: "",
      width: 18,
      render: (log) => {
        const checked = selected.includes(log.key);
        return (
          <button
            type="button"
            aria-label={`${log.to} 선택`}
            aria-pressed={checked}
            disabled={!isFailed(log)}
            onClick={() => toggle(log)}
            className={`flex h-[17px] w-[17px] items-center justify-center rounded-[5px] border-[1.5px] text-[11px] font-bold text-white disabled:cursor-not-allowed ${
              checked
                ? "border-ink bg-ink"
                : isFailed(log)
                  ? "cursor-pointer border-line-strong bg-white"
                  : "border-line-soft bg-white"
            }`}
          >
            {checked ? "✓" : ""}
          </button>
        );
      },
    },
    {
      key: "at",
      header: "발송일시",
      width: 120,
      render: (log) => (
        <span className="font-mono text-[12.5px] font-medium text-secondary">{log.at}</span>
      ),
    },
    {
      key: "to",
      header: "수신자",
      width: 220,
      render: (log) => <span className="text-[13.5px] font-medium text-ink">{log.to}</span>,
    },
    {
      key: "status",
      header: "상태",
      width: 76,
      render: (log) =>
        isResent(log) ? (
          <Badge tone="success">재발송 완료</Badge>
        ) : (
          <Badge tone={log.succeeded ? "success" : "danger"}>{log.succeeded ? "성공" : "실패"}</Badge>
        ),
    },
    {
      key: "reason",
      header: "실패 사유",
      render: (log) => (
        <span
          className={`block truncate text-[13.5px] font-medium ${
            isResent(log) ? "text-success" : log.succeeded ? "text-placeholder" : "text-alert-deep"
          }`}
        >
          {isResent(log) ? "재발송 처리됨" : log.reason || "—"}
        </span>
      ),
    },
    {
      key: "action",
      header: "액션",
      width: 84,
      align: "right",
      render: (log) =>
        isFailed(log) ? (
          <Button variant="secondary" size="sm" onClick={() => resend([log.key])}>
            재발송
          </Button>
        ) : isResent(log) ? (
          <Badge tone="success">재발송</Badge>
        ) : null,
    },
  ];

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          {FILTERS.map((f) => (
            <Chip
              key={f.key}
              label={f.label}
              active={filter === f.key}
              onClick={() => {
                setFilter(f.key);
                setPage(0);
              }}
            />
          ))}
        </div>
        <div className="flex flex-shrink-0 items-center gap-3">
          <span className="whitespace-nowrap text-[12.5px] font-medium text-placeholder">
            선택 <span className="font-mono font-semibold text-secondary">{selected.length}</span>건
          </span>
          <Button size="md" disabled={selected.length === 0} onClick={() => resend(selected)}>
            선택 일괄 재발송
          </Button>
        </div>
      </div>

      <AdminTable
        columns={columns}
        rows={rows}
        rowKey={(log) => log.key}
        rowClassName={(log) => (isFailed(log) ? "bg-danger-bg/30" : "bg-white")}
        emptyText="해당 조건의 발송 이력이 없습니다."
      />

      <div className="flex items-center justify-between gap-3">
        <span className="font-mono text-[12.5px] font-medium text-placeholder">
          총 {filtered.length}건 · 실패 {failedCount}건
        </span>
        <Pagination page={current} totalPages={totalPages} onChange={setPage} />
      </div>
    </div>
  );
}
