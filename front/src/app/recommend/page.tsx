import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import RecommendationFlow from "@/components/recommend/RecommendationFlow";

export default function RecommendPage() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="맞춤 문제 추천" subtitle="취약한 영역에 맞춘 문제를 만들어 드립니다" />
      <PageBody>
        <RecommendationFlow />
      </PageBody>
    </main>
  );
}
