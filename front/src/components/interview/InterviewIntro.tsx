"use client";

import { CATEGORIES } from "@/lib/badges";
import Chip from "@/components/ui/Chip";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

// 면접 시작 전: 카테고리 선택 + 진행 순서 안내
export default function InterviewIntro({
  cat,
  onSelectCat,
  onStart,
}: {
  cat: string;
  onSelectCat: (cat: string) => void;
  onStart: () => void;
}) {
  return (
    <Card className="flex flex-col gap-[22px] p-7">
      <div>
        <div className="mb-[11px] text-[13px] font-semibold text-muted">면접 카테고리</div>
        <div className="flex flex-wrap gap-2">
          {CATEGORIES.map((c) => (
            <Chip key={c} label={c} active={cat === c} onClick={() => onSelectCat(c)} />
          ))}
        </div>
      </div>
      <div className="flex flex-col gap-1.5 rounded-[12px] bg-subtle px-5 py-[18px]">
        <span className="text-xs font-semibold text-muted">오늘의 면접 진행 순서</span>
        <span className="text-[13.5px] leading-[1.7] text-body">
          질문 1개 제공 → 답변 입력 → AI 피드백 → 꼬리 질문 1개 → 개선 답변 예시 → 결과 저장
        </span>
      </div>
      <Button
        variant="ai"
        size="xl"
        onClick={onStart}
        className="flex items-center gap-2 self-start"
      >
        면접 시작
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
          <path d="M5 12h14M13 6l6 6-6 6" />
        </svg>
      </Button>
    </Card>
  );
}
