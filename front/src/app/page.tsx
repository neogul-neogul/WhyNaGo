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
import { syncStoredUser, useAuth } from "@/lib/auth";
import {
  guestPreviewGoal,
  guestPreviewStats,
  learningMenu as learningMenuBase,
  todayMetrics as todayMetricsBase,
} from "@/mocks/today";

interface TodaySummary {
  stats: LearningStats;
  /** 프로필 조회 전 잠깐 보여줄 기본값 (실패해도 화면을 막지 않기 위한 폴백) */
  dailyGoal: number;
  solvedToday: number;
  /** 오늘의 학습 완료 체크는 문제 개수가 아니라 오늘 푼 세션 수 기준이다 */
  sessionsToday: number;
  /** null이면 아직 조회 전 — 그동안은 메뉴 카드에 기존 더미 설명을 그대로 보여준다 */
  wrongNoteCount: number | null;
}

const EMPTY_SUMMARY: TodaySummary = {
  stats: { streakDays: 0, cumulativeDays: 0 },
  dailyGoal: 10,
  solvedToday: 0,
  sessionsToday: 0,
  wrongNoteCount: null,
};

export default function Home() {
  const loggedIn = useAuth();
  const [fetched, setFetched] = useState<TodaySummary | null>(null);

  // 로그아웃하면 조회해둔 지표를 즉시 감춘다
  const summary = loggedIn && fetched !== null ? fetched : EMPTY_SUMMARY;

  useEffect(() => {
    // 홈은 비로그인도 볼 수 있는 화면이라, 인증이 필요한 지표 조회는 로그인 상태에서만 한다
    if (!loggedIn) return;

    let cancelled = false;
    const today = todayDateKey();
    Promise.all([fetchStreak(), fetchDailyCounts(today, today), fetchWrongNotes(), fetchMyProfile()])
      .then(([streak, daily, wrongNotes, profile]) => {
        if (cancelled) return;
        setFetched({
          stats: streak,
          dailyGoal: profile.dailyGoal,
          solvedToday: daily[0]?.sessionCount ?? 0,
          sessionsToday: daily[0]?.sessionCount ?? 0,
          wrongNoteCount: wrongNotes.length,
        });
        syncStoredUser(profile);
      })
      .catch(() => {
        // 조회 실패는 오늘의 학습 화면 전체를 막지 않는다 — 각 지표는 기본값/더미로 표시된다
      });
    return () => {
      cancelled = true;
    };
  }, [loggedIn]);

  const todayGoal: TodayGoal = {
    target: summary.dailyGoal,
    current: summary.sessionsToday,
    completed: summary.sessionsToday >= summary.dailyGoal,
  };

  // "오늘 면접"·"오늘 오답 복습"은 아직 백엔드에 대응 개념이 없어(docs/DOMAIN.md 보류) 더미를 유지하고,
  // "오늘 푼 문제"만 실제 조회값으로 바꾼다.
  const todayMetrics: TodayMetric[] = todayMetricsBase.map((m) =>
    m.key === "solved" ? { ...m, value: String(summary.solvedToday) } : m,
  );

  // "오답노트 복습" 카드는 오답노트 도메인이 이미 연동돼 있으므로 실제 총 개수로 바꾼다.
  // 그 외(1일 1면접·모의 진단)는 백엔드가 없어 더미 설명을 그대로 둔다.
  const learningMenu: LearningMenuItem[] = learningMenuBase.map((item) =>
    item.key === "wrong" && summary.wrongNoteCount !== null
      ? { ...item, description: `복습할 문제 ${summary.wrongNoteCount}개` }
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
          <TodayBanner
            goal={loggedIn ? todayGoal : guestPreviewGoal}
            stats={loggedIn ? summary.stats : guestPreviewStats}
            preview={!loggedIn}
          />
          <TodayMetrics metrics={todayMetrics} />
          <LearningMenu items={learningMenu} />
        </div>
      </PageBody>
    </main>
  );
}
