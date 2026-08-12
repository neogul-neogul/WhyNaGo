import type { ProgressResponse } from "@/types";
import { orderedCategories, TIER_COLORS, TIER_LABELS } from "@/lib/progress";
import Card from "@/components/ui/Card";
import CategoryProgressList from "@/components/progress/CategoryProgressList";
import CategoryRadar from "@/components/progress/CategoryRadar";
import TierProgressBar from "@/components/progress/TierProgressBar";

// 진척도 대시보드 — 누적 점수·티어 + 카테고리별 점수(레이더)·진척도
export default function ProgressDashboard({ progress }: { progress: ProgressResponse }) {
  const categories = orderedCategories(progress);
  const tierColor = TIER_COLORS[progress.tier];

  return (
    <Card className="overflow-hidden">
      <div className="flex flex-wrap items-end justify-between gap-4 border-b border-line-card px-[30px] py-[22px]">
        <div>
          <div className="text-[11px] font-medium tracking-[0.14em] text-secondary">MY PROGRESS</div>
          <div className="mt-1.5 flex items-center gap-3 text-[23px] font-bold text-ink">
            전체 진척도
            <span
              className="rounded-[6px] border px-3 py-[5px] text-[12px] font-bold"
              style={{
                color: tierColor.line,
                background: tierColor.bg,
                borderColor: tierColor.border,
              }}
            >
              {TIER_LABELS[progress.tier]}
            </span>
          </div>
        </div>
        <div className="flex items-end gap-[34px]">
          <div>
            <div className="text-[11px] text-secondary">누적 점수</div>
            <div className="font-mono text-[26px] font-bold leading-[1.1] text-ink">
              {progress.score}
            </div>
          </div>
          <div>
            <div className="text-[11px] text-secondary">풀이 횟수</div>
            <div className="font-mono text-[26px] font-bold leading-[1.1] text-ink">
              {progress.totalQuestionCount}
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,430px)_minmax(0,1fr)]">
        <div className="border-b border-line-card px-[26px] py-[22px] lg:border-b-0 lg:border-r">
          <CategoryRadar categories={categories} />
        </div>
        <div className="flex flex-col gap-5 px-[26px] py-[20px]">
          <TierProgressBar progress={progress} />
          <CategoryProgressList categories={categories} />
        </div>
      </div>
    </Card>
  );
}
