"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type {
  EmailBatchExecutionDetailResponse,
  EmailBatchExecutionResponse,
  EmailBatchStatus,
} from "@/types";
import { ApiError } from "@/lib/api";
import {
  EMAIL_BATCH_STATUS_LABELS,
  EMAIL_BATCH_STATUS_TONES,
  fetchAdminEmailBatch,
  fetchAdminEmailBatches,
  formatFailureReasons,
  splitExecutedAt,
} from "@/lib/admin";
import Badge from "@/components/ui/Badge";
import Card from "@/components/ui/Card";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";

function StatusBadge({ status }: { status: EmailBatchStatus }) {
  return (
    <Badge tone={EMAIL_BATCH_STATUS_TONES[status]}>{EMAIL_BATCH_STATUS_LABELS[status]}</Badge>
  );
}

const COLUMNS: AdminColumn<EmailBatchExecutionResponse>[] = [
  {
    key: "date",
    header: "날짜",
    width: 120,
    render: (b) => (
      <span className="font-mono text-[13px] font-semibold text-ink">
        {splitExecutedAt(b.executedAt).date}
      </span>
    ),
  },
  {
    key: "at",
    header: "실행시각",
    width: 120,
    render: (b) => (
      <span className="font-mono text-[13px] font-medium text-secondary">
        {splitExecutedAt(b.executedAt).time}
      </span>
    ),
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
        className={`font-mono text-[13px] font-bold ${b.failureCount > 0 ? "text-danger" : "text-placeholder"}`}
      >
        {b.failureCount.toLocaleString()}건
      </span>
    ),
  },
];

export default function AdminEmailsPage() {
  const router = useRouter();
  const [page, setPage] = useState(0);
  const [batches, setBatches] = useState<EmailBatchExecutionResponse[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [latest, setLatest] = useState<EmailBatchExecutionDetailResponse | null>(null);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchAdminEmailBatches({ page })
      .then((response) => {
        if (cancelled) return;
        setBatches(response.content);
        setTotalPages(Math.max(1, response.totalPages));
        setLoaded(true);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "발송 이력을 불러오지 못했습니다.");
          setLoaded(true);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [page]);

  // 요약 카드의 실패 사유는 목록 응답에 없어 최신 배치만 단건으로 다시 조회한다.
  useEffect(() => {
    if (page !== 0 || batches.length === 0) return;

    let cancelled = false;
    fetchAdminEmailBatch(batches[0].id)
      .then((response) => {
        if (!cancelled) setLatest(response);
      })
      .catch(() => {
        // 요약은 보조 정보라 실패해도 목록은 그대로 보여준다
      });
    return () => {
      cancelled = true;
    };
  }, [page, batches]);

  return (
    <div className="flex w-full flex-col gap-[18px]">
      {error && (
        <div className="rounded-2xl border border-alert-line border-l-[3px] border-l-danger bg-white px-5 py-4 text-[12.5px] font-medium text-secondary">
          {error}
        </div>
      )}

      {latest ? (
        <LatestBatchSummary batch={latest} />
      ) : (
        loaded &&
        !error &&
        batches.length === 0 && (
          <Card className="p-[22px] text-[13.5px] text-soft">
            아직 실행된 발송 배치가 없습니다. 학습 리마인더 배치는 매일 21시에 실행됩니다.
          </Card>
        )
      )}

      <AdminTable
        columns={COLUMNS}
        rows={batches}
        rowKey={(b) => String(b.id)}
        onRowClick={(b) => router.push(`/admin/emails/${b.id}`)}
        emptyText={loaded ? "배치 실행 이력이 없습니다." : "불러오는 중..."}
        caption={{ left: "배치 실행 이력" }}
        footer={
          <div className="flex justify-center border-t border-line-card px-[22px] py-4">
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          </div>
        }
      />
    </div>
  );
}

function LatestBatchSummary({ batch }: { batch: EmailBatchExecutionDetailResponse }) {
  const { date, time } = splitExecutedAt(batch.executedAt);
  const failureSummary = formatFailureReasons(batch.failureReasons);
  const isToday = date === new Date().toISOString().slice(0, 10);

  return (
    <Card className="flex flex-col gap-3 p-[22px]">
      <span className="text-[13px] font-semibold text-muted">
        {isToday ? "오늘의 배치 요약" : "최근 배치 요약"}
      </span>

      <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2 xl:grid-cols-4">
        <SummaryTile label="발송 상태">
          <StatusBadge status={batch.status} />
        </SummaryTile>
        <SummaryTile label="실행 시각">
          <span className="font-mono text-[13px] font-bold text-ink">
            {date} {time}
          </span>
        </SummaryTile>
        <SummaryTile label="대상자 수">
          <span className="font-mono text-[13px] font-bold text-ink">
            {batch.totalTargetCount.toLocaleString()}건
          </span>
        </SummaryTile>
        <SummaryTile label="성공/실패">
          <span className="font-mono text-[13px] font-bold">
            <span className="text-success">{batch.successCount.toLocaleString()}</span>
            <span className="text-placeholder"> / </span>
            <span className="text-danger">{batch.failureCount.toLocaleString()}</span>
          </span>
        </SummaryTile>
      </div>

      {failureSummary && (
        <div className="rounded-[10px] border border-alert-line bg-danger-bg px-3.5 py-2.5 text-[12.5px] text-danger">
          <strong className="font-bold">실패 사유 요약:</strong> {failureSummary}
        </div>
      )}
    </Card>
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
