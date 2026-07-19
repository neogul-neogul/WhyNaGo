"use client";

import type { CatStat } from "@/types";
import { gradeColor } from "@/lib/badges";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

// 진단 결과: 전체 등급 + 취약 카테고리 + 카테고리별 등급/정답률
export default function MockResult({
  stats,
  onRetry,
}: {
  stats: CatStat[];
  onRetry: () => void;
}) {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex gap-4">
        <div className="flex min-w-[200px] flex-col items-center justify-center gap-1.5 rounded-[16px] bg-ink px-[30px] py-[26px] text-white">
          <span className="text-[12.5px] text-placeholder">전체 등급</span>
          <span className="font-mono text-[56px] font-bold leading-none text-success-bright">B</span>
          <span className="text-xs text-placeholder">
            이전 진단 C → <b className="text-success-bright">B</b> 상승
          </span>
        </div>
        <Card className="flex flex-1 flex-col gap-[13px] px-6 py-[22px]">
          <div className="text-[13px] font-semibold text-muted">취약 카테고리</div>
          <div className="flex flex-wrap gap-2.5">
            <span className="rounded-[9px] bg-danger-bg px-3.5 py-[7px] text-[13px] font-semibold text-danger">
              디자인패턴 · 58%
            </span>
            <span className="rounded-[9px] bg-alert-tint px-3.5 py-[7px] text-[13px] font-semibold text-alert">
              운영체제 · 63%
            </span>
          </div>
          <div className="mt-1 text-[13px] leading-[1.6] text-secondary">
            위 카테고리를 집중 학습하면 다음 진단 등급 상승에 도움이 됩니다.
          </div>
        </Card>
      </div>

      <Card className="px-6 py-6">
        <div className="mb-[18px] text-[13px] font-semibold text-muted">
          카테고리별 등급 · 정답률
        </div>
        <div className="flex flex-col gap-[15px]">
          {stats.map((c) => (
            <div key={c.name} className="flex items-center gap-4">
              <span className="w-20 flex-shrink-0 text-[13.5px] font-semibold">{c.name}</span>
              <span
                className="w-6 flex-shrink-0 font-mono text-[16px] font-bold"
                style={{ color: gradeColor(c.grade) }}
              >
                {c.grade}
              </span>
              <div className="h-[9px] flex-1 overflow-hidden rounded-md bg-neutral">
                <div
                  className="h-full rounded-md"
                  style={{ background: gradeColor(c.grade), width: `${c.acc}%` }}
                />
              </div>
              <span className="w-[42px] flex-shrink-0 text-right font-mono text-[13px] font-semibold text-secondary">
                {c.acc}%
              </span>
              <span className="w-[54px] flex-shrink-0 text-right text-xs text-axis">
                {c.change}
              </span>
            </div>
          ))}
        </div>
      </Card>

      <Button variant="secondary" size="xl" onClick={onRetry} className="self-start">
        다시 진단하기
      </Button>
    </div>
  );
}
