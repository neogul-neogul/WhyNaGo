"use client";

import { Suspense, useMemo, useState } from "react";
import { useParams, useSearchParams } from "next/navigation";
import type { AdminEmailRecipient } from "@/types";
import { adminEmailBatches, adminEmailRecipients } from "@/mocks/admin";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Pagination from "@/components/ui/Pagination";
import StatCard from "@/components/ui/StatCard";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";

const PAGE_SIZE = 8;

export default function AdminEmailBatchDetailPage() {
  // useSearchParams는 Suspense 경계 안에서만 쓸 수 있다 (목록의 "실패자만 재시도"가 ?failed=1로 들어온다)
  return (
    <Suspense fallback={null}>
      <BatchDetail />
    </Suspense>
  );
}

function BatchDetail() {
  const { date } = useParams<{ date: string }>();
  const searchParams = useSearchParams();
  const [failedOnly, setFailedOnly] = useState(searchParams.get("failed") === "1");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<string[]>([]);
  const [resent, setResent] = useState<string[]>([]);

  const batch = adminEmailBatches.find((b) => b.date === date);
  const recipients = useMemo(() => (batch ? adminEmailRecipients(batch) : []), [batch]);

  if (!batch) {
    return (
      <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
        발송 배치를 찾을 수 없습니다.
      </div>
    );
  }

  const isResent = (r: AdminEmailRecipient) => resent.includes(r.key);
  const isFailed = (r: AdminEmailRecipient) => !r.succeeded && !isResent(r);

  const toggle = (r: AdminEmailRecipient) => {
    if (!isFailed(r)) return;
    setSelected((prev) =>
      prev.includes(r.key) ? prev.filter((k) => k !== r.key) : [...prev, r.key],
    );
  };

  const resend = (keys: string[]) => {
    setResent((prev) => [...prev, ...keys]);
    setSelected((prev) => prev.filter((k) => !keys.includes(k)));
  };

  const filtered = recipients.filter((r) => !failedOnly || !r.succeeded);
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, totalPages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, (current + 1) * PAGE_SIZE);

  const columns: AdminColumn<AdminEmailRecipient>[] = [
    {
      key: "check",
      header: "",
      width: 18,
      render: (r) => {
        const checked = selected.includes(r.key);
        return (
          <button
            type="button"
            aria-label={`${r.email} 선택`}
            aria-pressed={checked}
            disabled={!isFailed(r)}
            onClick={() => toggle(r)}
            className={`flex h-[17px] w-[17px] items-center justify-center rounded-[5px] border-[1.5px] text-[11px] font-bold text-white disabled:cursor-not-allowed ${
              checked
                ? "border-ink bg-ink"
                : isFailed(r)
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
      key: "email",
      header: "수신자",
      render: (r) => (
        <span className="block truncate font-mono text-[13px] font-semibold text-ink">
          {r.email}
        </span>
      ),
    },
    {
      key: "sentAt",
      header: "발송 시각",
      width: 140,
      render: (r) => (
        <span className="font-mono text-[12.5px] font-medium text-secondary">{r.sentAt}</span>
      ),
    },
    {
      key: "status",
      header: "상태",
      width: 80,
      render: (r) =>
        isResent(r) ? (
          <Badge tone="success">재발송</Badge>
        ) : (
          <Badge tone={r.succeeded ? "success" : "danger"}>{r.succeeded ? "성공" : "실패"}</Badge>
        ),
    },
    {
      key: "reason",
      header: "실패 사유",
      width: 170,
      render: (r) => (
        <span
          className={`block truncate text-[12.5px] font-medium ${
            isResent(r) ? "text-success" : r.succeeded ? "text-icon" : "text-danger"
          }`}
        >
          {isResent(r) ? "재발송 처리됨" : r.reason || "—"}
        </span>
      ),
    },
    {
      key: "action",
      header: "액션",
      width: 76,
      align: "right",
      render: (r) =>
        isFailed(r) ? (
          <Button variant="secondary" size="sm" onClick={() => resend([r.key])}>
            재발송
          </Button>
        ) : null,
    },
  ];

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="발송일시">
          <span className="font-mono text-xl font-bold tracking-[-0.4px] text-ink">
            {batch.date} {batch.at}
          </span>
        </StatCard>
        <StatCard label="전체 발송 대상">
          <MetricValue value={batch.targetCount} />
        </StatCard>
        <StatCard label="성공">
          <MetricValue value={batch.successCount} className="text-success" />
        </StatCard>
        <StatCard label="실패">
          <MetricValue value={batch.failCount} className="text-danger" />
        </StatCard>
      </div>

      <AdminTable
        columns={columns}
        rows={rows}
        rowKey={(r) => r.key}
        rowClassName={(r) => (isFailed(r) ? "bg-danger-bg/30" : "bg-white")}
        emptyText="해당 조건의 발송 이력이 없습니다."
        caption={{
          left: (
            <span className="flex items-center gap-4">
              개별 발송 목록
              <button
                type="button"
                aria-pressed={failedOnly}
                onClick={() => {
                  setFailedOnly((v) => !v);
                  setPage(0);
                }}
                className="flex cursor-pointer items-center gap-2 text-[12.5px] font-semibold text-secondary transition-colors hover:text-ink"
              >
                <span
                  className={`flex h-[15px] w-[15px] items-center justify-center rounded-[4px] border-[1.5px] text-[10px] font-bold text-white ${
                    failedOnly ? "border-ink bg-ink" : "border-icon bg-white"
                  }`}
                >
                  {failedOnly ? "✓" : ""}
                </span>
                실패만 보기
              </button>
            </span>
          ),
          right: (
            <Button
              variant="secondary"
              size="md"
              disabled={selected.length === 0}
              onClick={() => resend(selected)}
            >
              선택한 {selected.length}건 재발송
            </Button>
          ),
        }}
        footer={
          <div className="flex justify-center border-t border-line-card px-[22px] py-4">
            <Pagination page={current} totalPages={totalPages} onChange={setPage} />
          </div>
        }
      />
    </div>
  );
}

function MetricValue({ value, className = "text-ink" }: { value: number; className?: string }) {
  return (
    <span className={`font-mono text-2xl font-bold tracking-[-0.5px] ${className}`}>
      {value.toLocaleString()}
      <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">건</span>
    </span>
  );
}
