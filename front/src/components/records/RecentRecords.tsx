import type { RecentRecordResponse } from "@/types";
import { CATEGORY_LABELS, TYPE_LABELS } from "@/lib/questions";
import { formatDuration, formatRecordDate } from "@/lib/records";
import Badge from "@/components/ui/Badge";

// 최근 학습 기록 리스트 — GET /api/learning-records/recent
export default function RecentRecords({ records }: { records: RecentRecordResponse[] }) {
  return (
    <div>
      <div className="mb-[13px] text-[13px] font-semibold text-muted">최근 학습 기록</div>
      {records.length === 0 ? (
        <div className="rounded-[13px] border border-line-card bg-white px-5 py-9 text-center text-[13.5px] text-soft">
          아직 학습 기록이 없습니다
        </div>
      ) : (
        <div className="flex flex-col gap-2.5">
          {records.map((r) => (
            <div
              key={r.sessionId}
              className="flex items-center gap-5 rounded-[13px] border border-line-card bg-white px-5 py-4"
            >
              <div className="flex w-24 flex-shrink-0 flex-col gap-0.5">
                <span className="font-mono text-[13.5px] font-semibold">{formatRecordDate(r.solvedAt)}</span>
                <span className="text-[11.5px] text-placeholder">{formatDuration(r.startedAt, r.solvedAt)}</span>
              </div>
              <Badge tone="accent" className="flex-shrink-0">{TYPE_LABELS[r.type]}</Badge>
              <div className="flex flex-1 flex-wrap items-center gap-1.5">
                <span className="rounded-[6px] bg-neutral px-[9px] py-[3px] text-xs text-secondary">
                  {CATEGORY_LABELS[r.category]}
                </span>
              </div>
              <div className="flex flex-shrink-0 items-center gap-[18px]">
                <span className="font-mono text-[13px] text-secondary">{r.totalCount}문제</span>
                <span className="font-mono text-[13px] font-semibold text-success">정답 {r.correctCount}</span>
                <span className="font-mono text-[13px] font-semibold text-danger">오답 {r.wrongCount}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
