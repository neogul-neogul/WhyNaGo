"use client";

import { useEffect, useState } from "react";
import type { DailyRecordCountResponse, RecentRecordResponse } from "@/types";
import { ApiError } from "@/lib/api";
import {
  defaultGrassRange,
  fetchDailyCounts,
  fetchRecentRecords,
  grassColors,
  levelGuide,
  toGrassWeeks,
} from "@/lib/records";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import GrassSection from "@/components/records/GrassSection";
import RecentRecords from "@/components/records/RecentRecords";

export default function RecordsPage() {
  const [recentRecords, setRecentRecords] = useState<RecentRecordResponse[] | null>(null);
  const [dailyCounts, setDailyCounts] = useState<DailyRecordCountResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const { from, to } = defaultGrassRange();
    Promise.all([fetchRecentRecords(), fetchDailyCounts(from, to)])
      .then(([recent, daily]) => {
        if (cancelled) return;
        setRecentRecords(recent);
        setDailyCounts(daily);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "학습 기록을 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="학습 기록" subtitle="매일의 학습을 잔디로 시각화합니다" />
      <PageBody>
        {error && (
          <div className="max-w-[620px] rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-danger">
            {error}
          </div>
        )}

        {!error && (!recentRecords || !dailyCounts) && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
            학습 기록을 불러오는 중…
          </div>
        )}

        {!error && recentRecords && dailyCounts && (
          <div className="flex flex-col gap-[18px]">
            <GrassSection
              weeks={toGrassWeeks(dailyCounts)}
              legendColors={grassColors}
              levelGuide={levelGuide}
            />
            <RecentRecords records={recentRecords} />
          </div>
        )}
      </PageBody>
    </main>
  );
}