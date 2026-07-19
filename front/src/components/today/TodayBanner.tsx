import type { LearningStats, TodayGoal } from "@/types";

// 오늘 상태 다크 배너 (목표 진행률 + 연속/누적 학습일)
export default function TodayBanner({
  goal,
  stats,
}: {
  goal: TodayGoal;
  stats: LearningStats;
}) {
  return (
    <div className="flex items-center justify-between gap-6 overflow-hidden rounded-[18px] bg-ink px-7 py-[26px] text-white">
      <div className="flex flex-col gap-2.5">
        <div>
          <span className="inline-flex items-center gap-1.5 rounded-[20px] bg-success-glow/[0.16] px-2.5 py-1 text-xs font-semibold text-success-bright">
            ● {goal.completed ? "오늘 학습 완료" : "오늘 학습 진행 중"}
          </span>
        </div>
        <div className="text-[23px] font-bold leading-[1.35] tracking-[-0.4px]">
          오늘도 꾸준히 이어가고 있어요
        </div>
        <div className="text-[13.5px] text-placeholder">
          최소 학습 목표{" "}
          <span className="font-mono font-semibold text-white">
            {goal.target}문제
          </span>{" "}
          중{" "}
          <span className="font-mono font-semibold text-success-bright">
            {goal.current}문제
          </span>{" "}
          완료
        </div>
        <div className="mt-1 h-[7px] w-[300px] max-w-full overflow-hidden rounded-md bg-white/[0.12]">
          <div
            className="h-full rounded-md bg-success-bright"
            style={{
              width: `${Math.min(100, (goal.current / goal.target) * 100)}%`,
            }}
          />
        </div>
      </div>
      <div className="flex gap-[30px]">
        <div className="flex flex-col items-center gap-[3px]">
          <span className="font-mono text-[34px] font-bold leading-none text-white">
            {stats.streakDays}
          </span>
          <span className="text-xs text-placeholder">연속 학습일</span>
        </div>
        <div className="w-px bg-white/[0.12]" />
        <div className="flex flex-col items-center gap-[3px]">
          <span className="font-mono text-[34px] font-bold leading-none text-white">
            {stats.cumulativeDays}
          </span>
          <span className="text-xs text-placeholder">누적 학습일</span>
        </div>
      </div>
    </div>
  );
}
