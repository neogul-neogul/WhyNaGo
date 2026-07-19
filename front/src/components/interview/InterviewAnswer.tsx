"use client";

import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

// 면접 진행 중: AI 질문 + 답변 입력
export default function InterviewAnswer({
  cat,
  question,
  answer,
  onChangeAnswer,
  onSubmit,
}: {
  cat: string;
  question: string;
  answer: string;
  onChangeAnswer: (answer: string) => void;
  onSubmit: () => void;
}) {
  return (
    <Card className="flex flex-col gap-[18px] p-7">
      <div className="flex items-center gap-2">
        <Badge tone="ai">{cat}</Badge>
        <span className="text-xs text-placeholder">질문 1 / 1</span>
      </div>
      <div className="flex items-start gap-[13px]">
        <div className="flex h-[34px] w-[34px] flex-shrink-0 items-center justify-center rounded-[9px] bg-ai text-xs font-bold text-white">
          AI
        </div>
        <div className="pt-1 text-[18px] font-semibold leading-[1.55] tracking-[-0.2px]">
          {question}
        </div>
      </div>
      <textarea
        value={answer}
        onChange={(e) => onChangeAnswer(e.target.value)}
        placeholder="답변을 입력하세요. 핵심 개념과 근거를 함께 설명하면 더 정확한 피드백을 받을 수 있어요."
        className="min-h-[150px] w-full resize-y rounded-[12px] border border-line-input bg-subtle p-4 text-[14.5px] leading-[1.65] text-ink outline-none"
      />
      <Button variant="ai" size="lg" onClick={onSubmit} className="self-end">
        답변 제출 · AI 피드백 받기
      </Button>
    </Card>
  );
}
