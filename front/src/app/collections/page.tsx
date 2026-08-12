"use client";

import { useRouter } from "next/navigation";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProblemSetList from "@/components/problemSets/ProblemSetList";

// 나의 문제집 목록 페이지 — "문제집" 탭
export default function CollectionsPage() {
  const router = useRouter();

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="나의 문제집" subtitle="풀고 싶은 문제를 모아 나만의 목록으로 관리하세요" />
      <PageBody>
        <ProblemSetList onOpen={(id) => router.push(`/collections/${id}`)} />
      </PageBody>
    </main>
  );
}
