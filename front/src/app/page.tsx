"use client";

import { useEffect, useState } from "react";
import type { LearningMenuItem, LearningStats, TodayGoal, TodayMetric } from "@/types";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import TodayBanner from "@/components/today/TodayBanner";
import TodayMetrics from "@/components/today/TodayMetrics";
import LearningMenu from "@/components/today/LearningMenu";
import { fetchDailyCounts, fetchStreak, todayDateKey } from "@/lib/records";
import { fetchWrongNotes } from "@/lib/wrongNotes";
import { fetchMyProfile } from "@/lib/user";
import { learningMenu as learningMenuBase, todayMetrics as todayMetricsBase } from "@/mocks/today";

// 프로필 조회 전 잠깐 보여줄 기본값 (실패해도 화면을 막지 않기 위한 폴백)
const DEFAULT_DAILY_GOAL = 10;

const EMPTY_STATS: LearningStats = { streakDays: 0, cumulativeDays: 0 };

export default function Home() {
  const [stats, setStats] = useState<LearningStats>(EMPTY_STATS);
  const [dailyGoal, setDailyGoal] = useState(DEFAULT_DAILY_GOAL);
  const [solvedToday, setSolvedToday] = useState(0);
  // 오늘의 학습 완료 체크는 문제 개수가 아니라 오늘 푼 세션 수 기준이다
  const [sessionsToday, setSessionsToday] = useState(0);
  // null이면 아직 조회 전 — 그동안은 메뉴 카드에 기존 더미 설명을 그대로 보여준다
  const [wrongNoteCount, setWrongNoteCount] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    const today = todayDateKey();
    Promise.all([fetchStreak(), fetchDailyCounts(today, today), fetchWrongNotes(), fetchMyProfile()])
      .then(([streak, daily, wrongNotes, profile]) => {
        if (cancelled) return;
        setStats(streak);
        setSolvedToday(daily[0]?.sessionCount ?? 0);
        setSessionsToday(daily[0]?.sessionCount ?? 0);
        setWrongNoteCount(wrongNotes.length);
        setDailyGoal(profile.dailyGoal);
      })
      .catch(() => {
        // 조회 실패는 오늘의 학습 화면 전체를 막지 않는다 — 각 지표는 기본값/더미로 표시된다
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const todayGoal: TodayGoal = {
    target: dailyGoal,
    current: sessionsToday,
    completed: sessionsToday >= dailyGoal,
  };

  // "오늘 면접"·"오늘 오답 복습"은 아직 백엔드에 대응 개념이 없어(docs/DOMAIN.md 보류) 더미를 유지하고,
  // "오늘 푼 문제"만 실제 조회값으로 바꾼다.
  const todayMetrics: TodayMetric[] = todayMetricsBase.map((m) =>
    m.key === "solved" ? { ...m, value: String(solvedToday) } : m,
  );

  // "오답노트 복습" 카드는 오답노트 도메인이 이미 연동돼 있으므로 실제 총 개수로 바꾼다.
  // 그 외(1일 1면접·모의 진단)는 백엔드가 없어 더미 설명을 그대로 둔다.
  const learningMenu: LearningMenuItem[] = learningMenuBase.map((item) =>
    item.key === "wrong" && wrongNoteCount !== null
      ? { ...item, description: `복습할 문제 ${wrongNoteCount}개` }
      : item,
  );

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
