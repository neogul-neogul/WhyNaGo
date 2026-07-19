import { weekRange, weeklyDays, weeklyRate, weeklyCards } from "@/mocks/progress";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import WeeklySummaryBanner from "@/components/weekly/WeeklySummaryBanner";
import WeeklyCards from "@/components/weekly/WeeklyCards";

export default function WeeklyPage() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="주간 리포트" subtitle="이번 주 학습 결과 요약" />
      <PageBody>
        <div className="flex max-w-[900px] flex-col gap-[18px]">
          <WeeklySummaryBanner range={weekRange} days={weeklyDays} rate={weeklyRate} />
          <WeeklyCards cards={weeklyCards} />

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
