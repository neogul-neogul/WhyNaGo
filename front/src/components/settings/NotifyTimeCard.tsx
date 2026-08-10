"use client";

import Card from "@/components/ui/Card";
import Chip from "@/components/ui/Chip";
import { REMIND_TIME_LABELS, labelToRemindTime, remindTimeLabel } from "@/lib/notification";

const TIME_LABELS = Object.values(REMIND_TIME_LABELS);

interface NotifyTimeCardProps {
  remindTime: string;
  onChange: (remindTime: string) => void;
}

// 매일 리마인드 시간 선택 카드
export default function NotifyTimeCard({ remindTime, onChange }: NotifyTimeCardProps) {
  const currentLabel = remindTimeLabel(remindTime);

  return (
    <Card className="flex items-center justify-between gap-4 px-[26px] py-[22px]">
      <div className="flex flex-col gap-[3px]">
        <span className="text-[14.5px] font-semibold">알림 시간</span>
        <span className="text-[12.5px] text-soft">매일 리마인드를 받을 시간을 설정합니다</span>
      </div>
      <div className="flex gap-2">
        {TIME_LABELS.map((label) => (
          <Chip
            key={label}
            label={label}
            active={currentLabel === label}
            onClick={() => {
              const time = labelToRemindTime(label);
              if (time) onChange(time);
            }}
          />
        ))}
      </div>
    </Card>
  );
}
