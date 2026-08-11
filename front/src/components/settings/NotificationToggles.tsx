"use client";

import Card from "@/components/ui/Card";
import Toggle from "@/components/ui/Toggle";
import type { NotificationSettingResponse } from "@/types";

type NotifField = keyof NotificationSettingResponse;

const NOTIF_DEFS: { field: NotifField; label: string; desc: string }[] = [
  { field: "everyDayRemind", label: "매일 학습 리마인드", desc: "오늘의 학습 기록이 없으면 알려드려요" },
];

interface NotificationTogglesProps {
  settings: NotificationSettingResponse;
  onToggle: (field: NotifField) => void;
}

// 알림 종류별 온/오프 토글 목록
export default function NotificationToggles({ settings, onToggle }: NotificationTogglesProps) {
  return (
    <Card className="px-[26px] py-2">
      {NOTIF_DEFS.map((n, i) => (
        <div
          key={n.field}
          className={`flex items-center justify-between gap-4 py-[18px] ${
            i === NOTIF_DEFS.length - 1 ? "" : "border-b border-line-soft"
          }`}
        >
          <div className="flex flex-col gap-[3px]">
            <span className="text-[14.5px] font-semibold">{n.label}</span>
            <span className="text-[12.5px] text-soft">{n.desc}</span>
          </div>
          <Toggle on={settings[n.field]} onToggle={() => onToggle(n.field)} label={n.label} />
        </div>
      ))}
    </Card>
  );
}
