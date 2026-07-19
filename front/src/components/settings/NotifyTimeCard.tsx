"use client";

import { useState } from "react";
import Card from "@/components/ui/Card";
import Chip from "@/components/ui/Chip";

const TIMES = ["오전 8시", "오후 1시", "오후 9시", "오후 11시"];

// 매일 리마인드 시간 선택 카드 (더미 상태는 섹션 내부에서만 관리)
export default function NotifyTimeCard() {
  const [time, setTime] = useState("오후 9시");

  return (
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
  );
}
