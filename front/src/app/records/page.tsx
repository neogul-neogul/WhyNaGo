"use client";

import { useState } from "react";
import { grassColors, grassWeeksData, levelGuide, recordsData } from "@/mocks/records";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";

type Sel = { level: number; count: number; color: string };

export default function RecordsPage() {
  const [sel, setSel] = useState<Sel | null>(null);
  const totalLearnDays = grassWeeksData.reduce(
    (a, w) => a + w.days.filter((d) => d.level > 0).length,
    0,
  );

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="학습 기록" subtitle="매일의 학습을 잔디로 시각화합니다" />
      <PageBody>
        <div className="flex flex-col gap-[18px]">
          {/* 잔디 */}
          <div className="rounded-[16px] border border-[#ECECE8] bg-white px-7 py-[26px]">
            <div className="mb-[18px] flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-baseline gap-2.5">
                <span className="text-[15px] font-bold">지난 1년 학습 기록</span>
                <span className="font-mono text-[13px] text-[#9A9A90]">{totalLearnDays}일 학습</span>
              </div>
              <div className="flex items-center gap-1.5 text-[11.5px] text-[#9A9A90]">
                적음
                {grassColors.map((c, i) => (
                  <span key={i} className="h-3 w-3 rounded-[3px]" style={{ background: c }} />
                ))}
                많음
              </div>
            </div>
            <div className="overflow-x-auto pb-1.5">
              <div className="flex min-w-max gap-[3px]">
                {grassWeeksData.map((week, wi) => (
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
            <div className="mt-[18px] flex flex-wrap gap-4 border-t border-[#F0F0EC] pt-4">
              {levelGuide.map((g, i) => (
                <div key={i} className="flex items-center gap-1.5">
                  <span className="h-3 w-3 flex-shrink-0 rounded-[3px]" style={{ background: g.color }} />
                  <span className="text-xs text-[#6B6B62]">{g.label}</span>
                </div>
              ))}
            </div>
          </div>

          {/* 선택된 날 */}
          {sel && (
            <div className="flex items-center gap-[18px] rounded-[14px] bg-[#1C1C1A] px-[22px] py-[18px] text-white">
              <div className="h-3.5 w-3.5 rounded-[4px]" style={{ background: sel.color }} />
              <span className="text-[14px] font-semibold">
                {sel.count > 0 ? `학습량 ${sel.level}단계` : "학습 기록 없음"}
              </span>
              <span className="text-[13px] text-[#A8A8A0]">
                {sel.count > 0
                  ? `${sel.count}문제 풀이 · 학습량 점수 +${sel.count + 8}`
                  : "이 날은 학습 기록이 없습니다"}
              </span>
            </div>
          )}

          {/* 최근 학습 기록 */}
          <div>
            <div className="mb-[13px] text-[13px] font-semibold text-[#8A8A80]">최근 학습 기록</div>
            <div className="flex flex-col gap-2.5">
              {recordsData.map((r, i) => (
                <div key={i} className="flex items-center gap-5 rounded-[13px] border border-[#ECECE8] bg-white px-5 py-4">
                  <div className="flex w-24 flex-shrink-0 flex-col gap-0.5">
                    <span className="font-mono text-[13.5px] font-semibold">{r.date}</span>
                    <span className="text-[11.5px] text-[#A8A8A0]">{r.time}</span>
                  </div>
                  <span className="flex-shrink-0 rounded-[6px] bg-[#EEF0FF] px-2.5 py-1 text-xs font-semibold text-[#4F46E5]">{r.method}</span>
                  <div className="flex flex-1 flex-wrap items-center gap-1.5">
                    {r.cats.map((c) => (
                      <span key={c} className="rounded-[6px] bg-[#F1F1ED] px-[9px] py-[3px] text-xs text-[#6B6B62]">{c}</span>
                    ))}
                  </div>
                  <div className="flex flex-shrink-0 items-center gap-[18px]">
                    <span className="font-mono text-[13px] text-[#6B6B62]">{r.solved}문제</span>
                    <span className="font-mono text-[13px] font-semibold text-[#16A34A]">정답 {r.correct}</span>
                    <span className="font-mono text-[13px] font-semibold text-[#DC2626]">오답 {r.wrong}</span>
                    <span className="rounded-[6px] bg-[#1C1C1A] px-2.5 py-1 text-xs font-semibold text-white">+{r.score}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </PageBody>
    </main>
  );
}
