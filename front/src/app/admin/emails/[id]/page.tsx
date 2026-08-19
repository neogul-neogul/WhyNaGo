"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import type { EmailBatchExecutionDetailResponse, EmailSendLogResponse } from "@/types";
import { ApiError } from "@/lib/api";
import {
  ADMIN_EMAIL_SEND_LOG_PAGE_SIZE,
  fetchAdminEmailBatch,
  fetchAdminEmailSendLogs,
  splitExecutedAt,
} from "@/lib/admin";
import Badge from "@/components/ui/Badge";
import Pagination from "@/components/ui/Pagination";
import StatCard from "@/components/ui/StatCard";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";

const COLUMNS: AdminColumn<EmailSendLogResponse>[] = [
  {
    key: "email",
    header: "수신자",
    render: (log) => (
      <span className="block truncate font-mono text-[13px] font-semibold text-ink">
        {log.recipientEmail ?? "—"}
      </span>
    ),
  },
  {
    key: "sentAt",
    header: "발송 시각",
    width: 140,
    render: (log) => (
      <span className="font-mono text-[12.5px] font-medium text-secondary">
        {log.sentAt.replace("T", " ").slice(0, 16)}
      </span>
    ),
  },
  {
    key: "status",
    header: "상태",
    width: 80,
    render: (log) => (
      <Badge tone={log.status === "SUCCESS" ? "success" : "danger"}>
        {log.status === "SUCCESS" ? "성공" : "실패"}
      </Badge>
    ),
  },
  {
    key: "reason",
    header: "실패 사유",
    width: 170,
    render: (log) => (
      <span
        className={`block truncate text-[12.5px] font-medium ${
          log.status === "SUCCESS" ? "text-icon" : "text-danger"
        }`}
      >
        {log.failureReason ?? "—"}
      </span>
    ),
  },
];

export default function AdminEmailBatchDetailPage() {
  const { id } = useParams<{ id: string }>();
  const executionId = Number(id);

  const [batch, setBatch] = useState<EmailBatchExecutionDetailResponse | null>(null);
  const [sendLogs, setSendLogs] = useState<EmailSendLogResponse[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [failedOnly, setFailedOnly] = useState(false);
  const [page, setPage] = useState(0);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchAdminEmailBatch(executionId)
      .then((response) => {
        if (!cancelled) setBatch(response);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "발송 배치를 찾을 수 없습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [executionId]);

  // 목록이 페이징되므로 "실패만 보기"는 화면에서 걸러내지 않고 서버 조건으로 다시 조회한다.
  useEffect(() => {
    let cancelled = false;
    fetchAdminEmailSendLogs(executionId, {
      status: failedOnly ? "FAILURE" : undefined,
      page,
      size: ADMIN_EMAIL_SEND_LOG_PAGE_SIZE,
    })
      .then((response) => {
        if (cancelled) return;
        setSendLogs(response.content);
        setTotalPages(Math.max(1, response.totalPages));
        setLoaded(true);
      })
      .catch(() => {
        if (!cancelled) setLoaded(true);
      });
    return () => {
      cancelled = true;
    };
  }, [executionId, failedOnly, page]);

  if (error) {
    return <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">{error}</div>;
  }

  const executedAt = batch ? splitExecutedAt(batch.executedAt) : null;

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="발송일시">
          <span className="font-mono text-xl font-bold tracking-[-0.4px] text-ink">
            {executedAt ? `${executedAt.date} ${executedAt.time}` : "—"}
          </span>
        </StatCard>
        <StatCard label="전체 발송 대상">
          <MetricValue value={batch?.totalTargetCount} />
        </StatCard>
        <StatCard label="성공">
          <MetricValue value={batch?.successCount} className="text-success" />
        </StatCard>
        <StatCard label="실패">
          <MetricValue value={batch?.failureCount} className="text-danger" />
        </StatCard>
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={sendLogs}
        rowKey={(log) => String(log.id)}
        rowClassName={(log) => (log.status === "FAILURE" ? "bg-danger-bg/30" : "bg-white")}
        emptyText={loaded ? "해당 조건의 발송 이력이 없습니다." : "불러오는 중..."}
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
        }}
        footer={
          <div className="flex justify-center border-t border-line-card px-[22px] py-4">
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          </div>
        }
      />
    </div>
  );
}

function MetricValue({ value, className = "text-ink" }: { value?: number; className?: string }) {
  if (value === undefined) {
    return <span className={`font-mono text-2xl font-bold tracking-[-0.5px] ${className}`}>—</span>;
  }

  return (
    <span className={`font-mono text-2xl font-bold tracking-[-0.5px] ${className}`}>
      {value.toLocaleString()}
      <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">건</span>
    </span>
  );
}
