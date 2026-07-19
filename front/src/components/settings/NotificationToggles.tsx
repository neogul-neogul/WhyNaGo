"use client";

import { useState } from "react";
import Card from "@/components/ui/Card";
import Toggle from "@/components/ui/Toggle";

type NotifKey = "daily" | "streak" | "wrong" | "interview" | "weekly";

const NOTIF_DEFS: { key: NotifKey; label: string; desc: string }[] = [
  { key: "daily", label: "매일 학습 리마인드", desc: "설정한 시간에 오늘의 학습을 알려드려요" },
  { key: "streak", label: "연속 학습 중단 방지", desc: "연속 학습 중인데 당일 기록이 없으면 저녁에 알림" },
  { key: "wrong", label: "오답 복습 알림", desc: "미복습 오답이 일정 개수 이상이면 알림" },
  { key: "interview", label: "1일 1면접 알림", desc: "오늘 면접을 아직 진행하지 않았을 때 알림" },
  { key: "weekly", label: "주간 리포트 수신", desc: "매주 월요일 학습 요약 이메일을 받아요" },
];

// 알림 종류별 온/오프 토글 목록 (더미 상태는 섹션 내부에서만 관리)
export default function NotificationToggles() {
  const [notif, setNotif] = useState<Record<NotifKey, boolean>>({
    daily: true,
    streak: true,
    wrong: true,
    interview: false,
    weekly: true,
  });

  return (
    <Card className="px-[26px] py-2">
      {NOTIF_DEFS.map((n, i) => (
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
          <Toggle
            on={notif[n.key]}
            onToggle={() => setNotif((s) => ({ ...s, [n.key]: !s[n.key] }))}
            label={n.label}
          />
        </div>
      ))}
    </Card>
  );
}
