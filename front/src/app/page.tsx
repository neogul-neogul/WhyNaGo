"use client";

import { useEffect, useState } from "react";
import type { LearningStats } from "@/types";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import TodayBanner from "@/components/today/TodayBanner";
import TodayMetrics from "@/components/today/TodayMetrics";
import LearningMenu from "@/components/today/LearningMenu";
import { fetchStreak } from "@/lib/records";
import { learningMenu, todayGoal, todayMetrics } from "@/mocks/today";

const EMPTY_STATS: LearningStats = { streakDays: 0, cumulativeDays: 0 };

export default function Home() {
  const [stats, setStats] = useState<LearningStats>(EMPTY_STATS);

  useEffect(() => {
    let cancelled = false;
    fetchStreak()
      .then((result) => {
        if (!cancelled) setStats(result);
      })
      .catch(() => {
        // 연속/누적 학습일 조회 실패는 오늘의 학습 화면 전체를 막지 않는다 — 배너는 0으로 표시된다
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader
        title="오늘의 학습"
        subtitle="매일의 학습을 한 화면에서 시작하세요"
      />
      <PageBody>
        <div className="flex flex-col gap-[22px]">
          <TodayBanner goal={todayGoal} stats={stats} />
          <TodayMetrics metrics={todayMetrics} />
          <LearningMenu items={learningMenu} />
        </div>
      </PageBody>
    </main>
  );
}
