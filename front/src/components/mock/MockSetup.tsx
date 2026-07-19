"use client";

import { CATEGORIES } from "@/lib/badges";
import Chip from "@/components/ui/Chip";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

// 진단 시작 전: 범위 선택 + 안내
export default function MockSetup({
  cat,
  onSelectCat,
  onStart,
}: {
  cat: string;
  onSelectCat: (cat: string) => void;
  onStart: () => void;
}) {
  return (
    <div className="flex max-w-[760px] flex-col gap-[18px]">
      <Card className="flex flex-col gap-[22px] px-7 py-[26px]">
        <div>
          <div className="mb-[11px] text-[13px] font-semibold text-muted">진단 범위</div>
          <div className="flex flex-wrap gap-2">
            {CATEGORIES.map((c) => (
              <Chip key={c} label={c} active={cat === c} onClick={() => onSelectCat(c)} />
            ))}
          </div>
        </div>
        <div className="rounded-[12px] bg-subtle px-5 py-[18px] text-[13.5px] leading-[1.7] text-body">
          난이도가 혼합되어 출제되며, 정답률과 문제 난이도를 기반으로 <b>A~D 등급</b>이
          계산됩니다. AI 분석은 사용하지 않습니다.
        </div>
      </Card>
      <Button size="xl" onClick={onStart} className="flex items-center gap-2 self-start">
        모의 진단 시작
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
          <path d="M5 12h14M13 6l6 6-6 6" />
        </svg>
      </Button>
    </div>
  );
}
