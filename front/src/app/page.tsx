import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import TodayBanner from "@/components/today/TodayBanner";
import TodayMetrics from "@/components/today/TodayMetrics";
import LearningMenu from "@/components/today/LearningMenu";
import { learningStats } from "@/mocks/user";
import { learningMenu, todayGoal, todayMetrics } from "@/mocks/today";

export default function Home() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader
        title="오늘의 학습"
        subtitle="매일의 학습을 한 화면에서 시작하세요"
      />
      <PageBody>
        <div className="flex flex-col gap-[22px]">
          <TodayBanner goal={todayGoal} stats={learningStats} />
          <TodayMetrics metrics={todayMetrics} />
          <LearningMenu items={learningMenu} />
        </div>
      </PageBody>
    </main>
  );
}
