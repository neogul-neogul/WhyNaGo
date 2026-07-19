import { progressMetrics } from "@/mocks/progress";
import { growthData } from "@/mocks/diagnosis";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProgressMetrics from "@/components/progress/ProgressMetrics";
import GrowthChart from "@/components/progress/GrowthChart";

export default function ProgressPage() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="진척도" subtitle="누적 학습 상태와 카테고리별 성장을 봅니다" />
      <PageBody>
        <div className="flex flex-col gap-[22px]">
          <ProgressMetrics metrics={progressMetrics} />
          <GrowthChart data={growthData} />
        </div>
      </PageBody>
    </main>
  );
}
