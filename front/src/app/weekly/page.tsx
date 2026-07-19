import { weekRange, weeklyDays, weeklyRate, weeklyCards } from "@/mocks/progress";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";

export default function WeeklyPage() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="주간 리포트" subtitle="이번 주 학습 결과 요약" />
      <PageBody>
        <div className="flex max-w-[900px] flex-col gap-[18px]">
          {/* 요약 배너 */}
          <div className="flex items-center justify-between gap-5 rounded-[16px] bg-ink px-7 py-6 text-white">
            <div className="flex flex-col gap-[5px]">
              <span className="text-[12.5px] text-placeholder">{weekRange}</span>
              <span className="text-[20px] font-bold">이번 주 학습 요약</span>
            </div>
            <div className="flex gap-7">
              <div className="flex flex-col items-center gap-0.5">
                <span className="font-mono text-[30px] font-bold">{weeklyDays}</span>
                <span className="text-[11.5px] text-placeholder">학습일 / 7</span>
              </div>
              <div className="w-px bg-white/[0.12]" />
              <div className="flex flex-col items-center gap-0.5">
                <span className="font-mono text-[30px] font-bold text-success-bright">{weeklyRate}%</span>
                <span className="text-[11.5px] text-placeholder">평균 정답률</span>
              </div>
            </div>
          </div>

          {/* 카드 */}
          <div className="grid grid-cols-2 gap-[13px]">
            {weeklyCards.map((w) => (
              <div key={w.label} className="flex items-center justify-between gap-3 rounded-[13px] border border-line-card bg-white px-5 py-[18px]">
                <div className="flex flex-col gap-[5px]">
                  <span className="text-[12.5px] font-medium text-muted">{w.label}</span>
                  <span className="text-[17px] font-bold">{w.value}</span>
                </div>
                <span className="font-mono text-[13px] font-semibold" style={{ color: w.deltaColor }}>{w.delta}</span>
              </div>
            ))}
          </div>

          <div className="flex items-center gap-[13px] rounded-[13px] border border-line-card bg-subtle px-[22px] py-[18px] text-secondary">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 16v-4M12 8h.01" />
            </svg>
            <span className="text-[13px] leading-[1.6]">
              주간 리포트는 AI 없이 정해진 템플릿과 데이터 계산으로 생성됩니다. 매주 월요일 이메일로도 발송돼요.
            </span>
          </div>
        </div>
      </PageBody>
    </main>
  );
}
