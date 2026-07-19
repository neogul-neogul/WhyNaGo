"use client";

import type { InterviewItem } from "@/types";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

// 면접 완료: 내 답변 + AI 피드백 + 꼬리 질문 + 개선 답변 예시
export default function InterviewFeedback({
  cat,
  interview,
  answer,
  onRestart,
  onSave,
}: {
  cat: string;
  interview: InterviewItem;
  answer: string;
  onRestart: () => void;
  onSave: () => void;
}) {
  return (
    <div className="flex flex-col gap-3.5">
      <Card className="flex flex-col gap-2 px-[26px] py-6">
        <Badge tone="ai" className="self-start">{cat}</Badge>
        <div className="mt-1 text-xs text-muted">질문</div>
        <div className="text-[16px] font-semibold leading-[1.55]">{interview.q}</div>
        <div className="mt-2 text-xs text-muted">내 답변</div>
        <div className="rounded-[10px] bg-subtle p-3.5 text-[14px] leading-[1.6] text-dim">
          {answer.trim() ? answer : "(답변 미입력)"}
        </div>
      </Card>

      <Card className="flex flex-col gap-[13px] px-[26px] py-6">
        <div className="flex items-center gap-2.5">
          <div className="flex h-7 w-7 items-center justify-center rounded-[8px] bg-ai text-[11px] font-bold text-white">
            AI
          </div>
          <span className="text-[15px] font-bold">피드백</span>
        </div>
        <div className="text-[14.5px] leading-[1.7] text-body">{interview.feedback}</div>
      </Card>

      <div className="flex flex-col gap-2.5 rounded-[16px] border border-ai-line bg-ai-bg px-[26px] py-6">
        <div className="text-[13px] font-bold text-ai-deep">꼬리 질문</div>
        <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">
          {interview.followup}
        </div>
      </div>

      <Card className="flex flex-col gap-[11px] px-[26px] py-6">
        <div className="text-[13px] font-bold text-success">개선 답변 예시</div>
        <div className="rounded-[10px] bg-success-bg p-4 text-[14.5px] leading-[1.75] text-body">
          {interview.improved}
        </div>
        <div className="mt-1 flex flex-wrap items-center gap-1.5">
          <span className="text-xs text-placeholder">관련 키워드</span>
          {interview.keywords.map((kw) => (
            <span key={kw} className="rounded-[20px] bg-neutral px-2.5 py-1 text-xs font-medium text-secondary">
              {kw}
            </span>
          ))}
        </div>
      </Card>

      <div className="flex gap-3">
        <Button variant="secondary" size="xl" onClick={onRestart} className="flex-1">
          처음으로
        </Button>
        <Button variant="success" size="xl" onClick={onSave} className="flex-1">
          면접 결과 저장
        </Button>
      </div>
    </div>
  );
}
