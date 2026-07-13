"use client";

import { useState } from "react";
import { catStatsData } from "@/mocks/diagnosis";
import { CATEGORIES, gradeColor } from "@/lib/badges";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";

export default function MockPage() {
  const [done, setDone] = useState(false);
  const [cat, setCat] = useState("전체");

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="모의 진단" subtitle="정답률과 난이도 기반으로 실력 등급을 확인합니다" />
      <PageBody>
        <div className="flex flex-col gap-[18px]">
          {!done ? (
            <div className="flex max-w-[760px] flex-col gap-[18px]">
              <div className="flex flex-col gap-[22px] rounded-[16px] border border-[#ECECE8] bg-white px-7 py-[26px]">
                <div>
                  <div className="mb-[11px] text-[13px] font-semibold text-[#8A8A80]">진단 범위</div>
                  <div className="flex flex-wrap gap-2">
                    {CATEGORIES.map((c) => (
                      <Chip key={c} label={c} active={cat === c} onClick={() => setCat(c)} />
                    ))}
                  </div>
                </div>
                <div className="rounded-[12px] bg-[#FAFAF7] px-5 py-[18px] text-[13.5px] leading-[1.7] text-[#3A3A34]">
                  난이도가 혼합되어 출제되며, 정답률과 문제 난이도를 기반으로 <b>A~D 등급</b>이 계산됩니다. AI 분석은 사용하지 않습니다.
                </div>
              </div>
              <button
                type="button"
                onClick={() => setDone(true)}
                className="flex items-center gap-2 self-start rounded-[11px] bg-[#1C1C1A] px-7 py-[13px] text-[15px] font-semibold text-white"
              >
                모의 진단 시작
                <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
                  <path d="M5 12h14M13 6l6 6-6 6" />
                </svg>
              </button>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              <div className="flex gap-4">
                <div className="flex min-w-[200px] flex-col items-center justify-center gap-1.5 rounded-[16px] bg-[#1C1C1A] px-[30px] py-[26px] text-white">
                  <span className="text-[12.5px] text-[#A8A8A0]">전체 등급</span>
                  <span className="font-mono text-[56px] font-bold leading-none text-[#5DDC8A]">B</span>
                  <span className="text-xs text-[#A8A8A0]">
                    이전 진단 C → <b className="text-[#5DDC8A]">B</b> 상승
                  </span>
                </div>
                <div className="flex flex-1 flex-col gap-[13px] rounded-[16px] border border-[#ECECE8] bg-white px-6 py-[22px]">
                  <div className="text-[13px] font-semibold text-[#8A8A80]">취약 카테고리</div>
                  <div className="flex flex-wrap gap-2.5">
                    <span className="rounded-[9px] bg-[#FEF2F2] px-3.5 py-[7px] text-[13px] font-semibold text-[#DC2626]">디자인패턴 · 58%</span>
                    <span className="rounded-[9px] bg-[#FEF2E8] px-3.5 py-[7px] text-[13px] font-semibold text-[#C2410C]">운영체제 · 63%</span>
                  </div>
                  <div className="mt-1 text-[13px] leading-[1.6] text-[#6B6B62]">
                    위 카테고리를 집중 학습하면 다음 진단 등급 상승에 도움이 됩니다.
                  </div>
                </div>
              </div>

              <div className="rounded-[16px] border border-[#ECECE8] bg-white px-6 py-6">
                <div className="mb-[18px] text-[13px] font-semibold text-[#8A8A80]">카테고리별 등급 · 정답률</div>
                <div className="flex flex-col gap-[15px]">
                  {catStatsData.map((c) => (
                    <div key={c.name} className="flex items-center gap-4">
                      <span className="w-20 flex-shrink-0 text-[13.5px] font-semibold">{c.name}</span>
                      <span className="w-6 flex-shrink-0 font-mono text-[16px] font-bold" style={{ color: gradeColor(c.grade) }}>{c.grade}</span>
                      <div className="h-[9px] flex-1 overflow-hidden rounded-md bg-[#F1F1ED]">
                        <div className="h-full rounded-md" style={{ background: gradeColor(c.grade), width: `${c.acc}%` }} />
                      </div>
                      <span className="w-[42px] flex-shrink-0 text-right font-mono text-[13px] font-semibold text-[#6B6B62]">{c.acc}%</span>
                      <span className="w-[54px] flex-shrink-0 text-right text-xs text-[#B0B0A6]">{c.change}</span>
                    </div>
                  ))}
                </div>
              </div>

              <button
                type="button"
                onClick={() => setDone(false)}
                className="self-start rounded-[11px] border border-[#DCDCD4] bg-white px-6 py-3 text-[14px] font-semibold text-[#1C1C1A]"
              >
                다시 진단하기
              </button>
            </div>
          )}
        </div>
      </PageBody>
    </main>
  );
}
