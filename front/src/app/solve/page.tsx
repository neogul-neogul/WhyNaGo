"use client";

import { useRouter } from "next/navigation";
import type { QuestionResponse } from "@/types";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProblemBank from "@/components/solve/ProblemBank";

// 문제 목록 페이지 — 문제를 고르면 상세(풀이) 페이지(/solve/[id])로 이동한다
export default function SolvePage() {
  const router = useRouter();

  const startProblem = (q: QuestionResponse) => {
    router.push(`/solve/${q.id}`);
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader
        title="문제 풀이"
        subtitle="문제은행에서 풀고 싶은 문제를 골라 바로 도전하세요"
      />
      <PageBody>
        <ProblemBank onStart={startProblem} />
      </PageBody>
    </main>
  );
}
