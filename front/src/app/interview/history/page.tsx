"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type { InterviewHistoryResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { useAuth, useHydrated } from "@/lib/auth";
import { fetchInterviewHistory } from "@/lib/interviews";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import LoginRequiredGate from "@/components/layout/LoginRequiredGate";
import InterviewHistoryCard from "@/components/interview/InterviewHistoryCard";

// 면접 기록 목록 — 완료된 면접 전체(정답/오답 필터링 없음)
export default function InterviewHistoryPage() {
  const router = useRouter();
  const hydrated = useHydrated();
  const loggedIn = useAuth();

  const [history, setHistory] = useState<InterviewHistoryResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!hydrated || !loggedIn) return;
    let cancelled = false;
    fetchInterviewHistory()
      .then((list) => {
        if (!cancelled) setHistory(list);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "면접 기록을 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [hydrated, loggedIn]);

  const body = () => {
    if (!hydrated) return <Placeholder text="불러오는 중…" />;
    if (!loggedIn) return <LoginRequiredGate />;
    if (error) return <div className="px-[22px] py-10 text-center text-[13.5px] text-danger">{error}</div>;
    if (history === null) return <Placeholder text="면접 기록을 불러오는 중…" />;
    if (history.length === 0) {
      return (
        <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
          아직 완료한 면접이 없습니다
        </div>
      );
    }

    return (
      <div className="flex flex-col gap-2.5">
        {history.map((item) => (
          <InterviewHistoryCard
            key={item.interviewId}
            history={item}
            onOpen={() => router.push(`/interview/result/${item.interviewId}`)}
          />
        ))}
      </div>
    );
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="면접 기록" subtitle="지금까지 완료한 1일 1면접을 모두 확인하세요" />
      <PageBody>
        <div className="mx-auto max-w-[860px]">{body()}</div>
      </PageBody>
    </main>
  );
}

function Placeholder({ text }: { text: string }) {
  return <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">{text}</div>;
}
