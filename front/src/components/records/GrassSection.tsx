"use client";

import { useState } from "react";
import type { GrassDay } from "@/types";
import Card from "@/components/ui/Card";

type Sel = { level: number; count: number; color: string };

// 지난 1년 학습 잔디 + 선택한 날 상세 배너 (선택 상태는 이 섹션 안에서만 쓰인다)
export default function GrassSection({
  weeks,
  legendColors,
  levelGuide,
}: {
  weeks: { days: GrassDay[] }[];
  legendColors: string[];
  levelGuide: { color: string; label: string }[];
}) {
  const [sel, setSel] = useState<Sel | null>(null);
  const totalLearnDays = weeks.reduce(
    (a, w) => a + w.days.filter((d) => d.level > 0).length,
    0,
  );

  return (
    <>
      {/* 잔디 */}
      <Card className="px-7 py-[26px]">
        <div className="mb-[18px] flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-baseline gap-2.5">
            <span className="text-[15px] font-bold">지난 1년 학습 기록</span>
            <span className="font-mono text-[13px] text-soft">{totalLearnDays}일 학습</span>
          </div>
          <div className="flex items-center gap-1.5 text-[11.5px] text-soft">
            적음
            {legendColors.map((c, i) => (
              <span key={i} className="h-3 w-3 rounded-[3px]" style={{ background: c }} />
            ))}
            많음
          </div>
        </div>
        <div className="overflow-x-auto pb-1.5">
          <div className="flex min-w-max gap-[3px]">
            {weeks.map((week, wi) => (
              <div key={wi} className="flex flex-col gap-[3px]">
                {week.days.map((d, di) => (
                  <div
                    key={di}
                    title={d.count > 0 ? `${d.count}문제 풀이` : "학습 없음"}
                    onClick={() => setSel({ level: d.level, count: d.count, color: d.color })}
                    className="h-[13px] w-[13px] cursor-pointer rounded-[3px]"
                    style={{ background: d.color }}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>
        <div className="mt-[18px] flex flex-wrap gap-4 border-t border-line-soft pt-4">
          {levelGuide.map((g, i) => (
            <div key={i} className="flex items-center gap-1.5">
              <span className="h-3 w-3 flex-shrink-0 rounded-[3px]" style={{ background: g.color }} />
              <span className="text-xs text-secondary">{g.label}</span>
            </div>
          ))}
        </div>
      </Card>

      {/* 선택된 날 */}
      {sel && (
        <div className="flex items-center gap-[18px] rounded-[14px] bg-ink px-[22px] py-[18px] text-white">
          <div className="h-3.5 w-3.5 rounded-[4px]" style={{ background: sel.color }} />
          <span className="text-[14px] font-semibold">
            {sel.count > 0 ? `학습량 ${sel.level}단계` : "학습 기록 없음"}
          </span>
          <span className="text-[13px] text-placeholder">
            {sel.count > 0
              ? `${sel.count}문제 풀이 · 학습량 점수 +${sel.count + 8}`
              : "이 날은 학습 기록이 없습니다"}
          </span>
        </div>
      )}
    </>
  );
}
