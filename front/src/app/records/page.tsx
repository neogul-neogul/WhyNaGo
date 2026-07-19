import { grassColors, grassWeeksData, levelGuide, recordsData } from "@/mocks/records";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import GrassSection from "@/components/records/GrassSection";
import RecentRecords from "@/components/records/RecentRecords";

export default function RecordsPage() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="학습 기록" subtitle="매일의 학습을 잔디로 시각화합니다" />
      <PageBody>
        <div className="flex flex-col gap-[18px]">
          <GrassSection
            weeks={grassWeeksData}
            legendColors={grassColors}
            levelGuide={levelGuide}
          />
          <RecentRecords records={recordsData} />
        </div>
      </PageBody>
    </main>
  );
}
