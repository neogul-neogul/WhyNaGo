"use client";

import { useState } from "react";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";
import Card from "@/components/ui/Card";

type NotifKey = "daily" | "streak" | "wrong" | "interview" | "weekly";

const NOTIF_DEFS: { key: NotifKey; label: string; desc: string }[] = [
  { key: "daily", label: "매일 학습 리마인드", desc: "설정한 시간에 오늘의 학습을 알려드려요" },
  { key: "streak", label: "연속 학습 중단 방지", desc: "연속 학습 중인데 당일 기록이 없으면 저녁에 알림" },
  { key: "wrong", label: "오답 복습 알림", desc: "미복습 오답이 일정 개수 이상이면 알림" },
  { key: "interview", label: "1일 1면접 알림", desc: "오늘 면접을 아직 진행하지 않았을 때 알림" },
  { key: "weekly", label: "주간 리포트 수신", desc: "매주 월요일 학습 요약 이메일을 받아요" },
];

const TIMES = ["오전 8시", "오후 1시", "오후 9시", "오후 11시"];

export default function SettingsPage() {
  const [notif, setNotif] = useState<Record<NotifKey, boolean>>({
    daily: true,
    streak: true,
    wrong: true,
    interview: false,
    weekly: true,
  });
  const [time, setTime] = useState("오후 9시");

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="알림 설정" subtitle="학습 지속을 돕는 이메일 알림을 설정합니다" />
      <PageBody>
        <div className="flex max-w-[720px] flex-col gap-[18px]">
          {/* 알림 토글 */}
          <Card className="px-[26px] py-2">
            {NOTIF_DEFS.map((n, i) => {
              const on = notif[n.key];
              return (
                <div
                  key={n.key}
                  className={`flex items-center justify-between gap-4 py-[18px] ${
                    i === NOTIF_DEFS.length - 1 ? "" : "border-b border-line-soft"
                  }`}
                >
                  <div className="flex flex-col gap-[3px]">
                    <span className="text-[14.5px] font-semibold">{n.label}</span>
                    <span className="text-[12.5px] text-soft">{n.desc}</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setNotif((s) => ({ ...s, [n.key]: !s[n.key] }))}
                    className={`relative h-[25px] w-11 flex-shrink-0 rounded-[20px] transition-colors ${
                      on ? "bg-success" : "bg-line-strong"
                    }`}
                  >
                    <span
                      className="absolute top-[3px] h-[19px] w-[19px] rounded-full bg-white shadow-[0_1px_2px_rgba(0,0,0,0.2)] transition-all"
                      style={{ left: on ? "22px" : "3px" }}
                    />
                  </button>
                </div>
              );
            })}
          </Card>

          {/* 알림 시간 */}
          <Card className="flex items-center justify-between gap-4 px-[26px] py-[22px]">
            <div className="flex flex-col gap-[3px]">
              <span className="text-[14.5px] font-semibold">알림 시간</span>
              <span className="text-[12.5px] text-soft">매일 리마인드를 받을 시간을 설정합니다</span>
            </div>
            <div className="flex gap-2">
              {TIMES.map((t) => (
                <Chip key={t} label={t} active={time === t} onClick={() => setTime(t)} />
              ))}
            </div>
          </Card>

          {/* 발송 조건 */}
          <div className="flex flex-col gap-[11px] rounded-[13px] border border-line-card bg-subtle px-6 py-5">
            <span className="text-[13px] font-semibold text-muted">발송 조건</span>
            <div className="flex flex-col gap-2">
              {[
                "오늘 학습 기록이 없으면 지정한 시간에 리마인드 발송",
                "연속 학습 중인데 당일 기록이 없으면 저녁에 중단 방지 알림",
                "미복습 오답이 일정 개수 이상이면 복습 알림",
                "매주 월요일에 주간 리포트 이메일 발송",
              ].map((c) => (
                <div key={c} className="flex items-start gap-2.5 text-[13px] leading-[1.5] text-dim">
                  <span className="text-alert">•</span>
                  {c}
                </div>
              ))}
            </div>
          </div>
        </div>
      </PageBody>
    </main>
  );
}
