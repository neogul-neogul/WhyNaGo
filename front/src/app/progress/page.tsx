"use client";

import { useEffect, useState } from "react";
import type { MasteryResponse, ProgressResponse, ProgressSummaryResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchProgress, fetchProgressSummary, toProgressMetrics } from "@/lib/progress";
import { fetchMastery, hasMastery } from "@/lib/mastery";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProgressMetrics from "@/components/progress/ProgressMetrics";
import ProgressDashboard from "@/components/progress/ProgressDashboard";
import MasteryDashboard from "@/components/progress/MasteryDashboard";

export default function ProgressPage() {
  const [summary, setSummary] = useState<ProgressSummaryResponse | null>(null);
  const [progress, setProgress] = useState<ProgressResponse | null>(null);
  const [mastery, setMastery] = useState<MasteryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([fetchProgressSummary(), fetchProgress(), fetchMastery()])
      .then(([summaryResult, progressResult, masteryResult]) => {
        if (cancelled) return;
        setSummary(summaryResult);
        setProgress(progressResult);
        setMastery(masteryResult);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "진척도를 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="진척도" subtitle="누적 학습 상태와 카테고리별 성장을 봅니다" />
      <PageBody>
        {error && (
          <div className="max-w-[620px] rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-danger">
            {error}
          </div>
        )}

        {!error && (!summary || !progress) && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
            진척도를 불러오는 중…
          </div>
        )}

        {!error && summary && progress && (
          <div className="flex flex-col gap-[22px]">
            <ProgressMetrics metrics={toProgressMetrics(summary)} />
            <ProgressDashboard progress={progress} />
            {/* 판정 이력이 없으면 빈 카드를 띄우지 않는다. 아직 서술형을 풀지 않은 사용자다. */}
            {mastery && hasMastery(mastery) && <MasteryDashboard mastery={mastery} />}
          </div>
        )}
      </PageBody>
    </main>
  );
}
