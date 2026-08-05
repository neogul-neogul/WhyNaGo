import type { LearningStats, TodayGoal } from "@/types";

// 오늘 상태 다크 배너 (목표 진행률 + 연속/누적 학습일)
// preview: 비로그인용 예시 화면 — goal/stats는 더미이고 안내 카드를 얹어 보여준다
export default function TodayBanner({
  goal,
  stats,
  preview = false,
}: {
  goal: TodayGoal;
  stats: LearningStats;
  preview?: boolean;
}) {
  return (
    <div className="relative flex flex-col gap-5 overflow-hidden rounded-[18px] bg-ink px-7 py-[26px] text-white">
      <div className={`flex flex-col gap-5 ${preview ? "opacity-75 blur-[1.5px]" : ""}`}>
        <div className="flex items-center justify-between gap-6">
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
        {/* 진행률 바 — 배너 전체 폭(누적 학습일 아래까지)을 채운다 */}
        <div className="h-[7px] w-full overflow-hidden rounded-md bg-white/[0.12]">
          <div
            className="h-full rounded-md bg-success-bright"
            style={{
              width: `${Math.min(100, (goal.current / goal.target) * 100)}%`,
            }}
          />
        </div>
      </div>

      {preview && (
        <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-ink/35 px-6 text-center">
          <span className="rounded-[20px] bg-white/15 px-3 py-1 text-[13px] font-semibold text-white">
            예시 화면
          </span>
          <div className="text-[19px] font-bold leading-[1.4] text-white">
            로그인하면 오늘의 기록이 여기에 쌓여요
          </div>
          <div className="text-[15px] text-placeholder">
            연속 학습일과 목표 달성률을 매일 확인할 수 있어요
          </div>
        </div>
      )}
    </div>
  );
}
