import type { RecordItem } from "@/types";
import Badge from "@/components/ui/Badge";

// 최근 학습 기록 리스트
export default function RecentRecords({ records }: { records: RecordItem[] }) {
  return (
    <div>
      <div className="mb-[13px] text-[13px] font-semibold text-muted">최근 학습 기록</div>
      <div className="flex flex-col gap-2.5">
        {records.map((r, i) => (
          <div
            key={i}
            className="flex items-center gap-5 rounded-[13px] border border-line-card bg-white px-5 py-4"
          >
            <div className="flex w-24 flex-shrink-0 flex-col gap-0.5">
              <span className="font-mono text-[13.5px] font-semibold">{r.date}</span>
              <span className="text-[11.5px] text-placeholder">{r.time}</span>
            </div>
            <Badge tone="accent" className="flex-shrink-0">{r.method}</Badge>
            <div className="flex flex-1 flex-wrap items-center gap-1.5">
              {r.cats.map((c) => (
                <span key={c} className="rounded-[6px] bg-neutral px-[9px] py-[3px] text-xs text-secondary">
                  {c}
                </span>
              ))}
            </div>
            <div className="flex flex-shrink-0 items-center gap-[18px]">
              <span className="font-mono text-[13px] text-secondary">{r.solved}문제</span>
              <span className="font-mono text-[13px] font-semibold text-success">정답 {r.correct}</span>
              <span className="font-mono text-[13px] font-semibold text-danger">오답 {r.wrong}</span>
              <Badge tone="ink">+{r.score}</Badge>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
