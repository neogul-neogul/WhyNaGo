"use client";

import Link from "next/link";
import type { InterviewResultResponse } from "@/types";
import { CATEGORY_LABELS } from "@/lib/questions";
import {
  formatDurationSeconds,
  formatInterviewDate,
  interviewItemLabel,
} from "@/lib/interviews";
import Badge from "@/components/ui/Badge";
import Card, { CardHeader } from "@/components/ui/Card";

// 면접 결과.
// 서술형 풀이에서는 감췄던 modelAnswer를 여기서는 노출한다 — 결과 제공이 이 화면의 목적이다.
export default function InterviewResult({ result }: { result: InterviewResultResponse }) {
  const wrongCount = result.totalCount - result.correctCount;

  return (
    <div className="flex flex-col gap-[18px]">
      {/* 요약 */}
      <Card className="overflow-hidden">
        <CardHeader className="gap-2.5">
          <Badge tone="ai">1일 1면접</Badge>
          <Badge tone="neutral">{CATEGORY_LABELS[result.category]}</Badge>
          <span className="ml-auto text-[12.5px] font-semibold text-soft">
            {formatInterviewDate(result.interviewDate)}
          </span>
        </CardHeader>
        <div className="grid grid-cols-3 gap-px bg-line-card">
          <Summary
            label="통과"
            value={`${result.correctCount} / ${result.totalCount}`}
            tone={wrongCount === 0 ? "success" : "default"}
          />
          <Summary label="소요 시간" value={formatDurationSeconds(result.durationSeconds)} />
          <Summary
            label="화면 이탈"
            value={`${result.focusLossCount}회`}
            tone={result.focusLossCount > 0 ? "alert" : "default"}
          />
        </div>
      </Card>

      {/* 오답노트 안내 */}
      {wrongCount > 0 && (
        <div className="flex items-center gap-3 rounded-[13px] border border-alert-line bg-alert-bg px-[18px] py-3.5">
          <span className="text-[13px] leading-[1.5] text-alert-deep">
            미통과 문항이 있어 <b>오답노트에 저장</b>되었어요. 복습하면서 답변을 다듬어 보세요.
          </span>
          <Link
            href="/wrong"
            className="ml-auto flex-shrink-0 rounded-[10px] border border-line-strong bg-white px-4 py-2 text-[13px] font-semibold text-ink transition-colors hover:border-ink"
          >
            오답노트
          </Link>
        </div>
      )}

      {/* 문항별 결과 */}
      {result.items.map((item, i) => (
        <Card key={item.sequence} className="overflow-hidden">
          <CardHeader className="gap-2">
            <Badge tone="accent" size="xs">
              {interviewItemLabel(i)}
            </Badge>
            <span
              className={`ml-auto text-[12.5px] font-bold ${
                item.isCorrect ? "text-success" : "text-danger"
              }`}
            >
              {item.isCorrect ? "✓ 통과" : "✕ 미통과"}
            </span>
          </CardHeader>

          <div className="flex flex-col gap-3.5 px-[22px] py-5">
            <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">
              {item.questionText}
            </div>

            <Section label="내 답변">
              {item.userAnswer ? (
                <span className="whitespace-pre-wrap">{item.userAnswer}</span>
              ) : (
                <span className="text-soft">
                  답변을 작성하지 못한 채 제한 시간이 만료되었어요.
                </span>
              )}
            </Section>

            <div className="rounded-[12px] border border-alert-line bg-paper px-4 py-3.5">
              <div className="mb-1.5 text-[11.5px] font-bold text-alert">AI 피드백</div>
              <div className="whitespace-pre-wrap text-[13.5px] leading-[1.65] text-alert-deep">
                {item.feedback}
              </div>
            </div>

            <div className="rounded-[12px] border border-success-bg bg-success-bg/40 px-4 py-3.5">
              <div className="mb-1.5 text-[11.5px] font-bold text-success">모범답안</div>
              <div className="whitespace-pre-wrap text-[13.5px] leading-[1.65] text-body">
                {item.modelAnswer}
              </div>
            </div>
          </div>
        </Card>
      ))}

      <div className="flex gap-2">
        <Link
          href="/solve"
          className="rounded-[11px] bg-ink px-7 py-[13px] text-[15px] font-semibold text-white transition-colors hover:bg-ink-hover"
        >
          문제 풀이 이어가기
        </Link>
        <Link
          href="/"
          className="rounded-[11px] border border-line-strong bg-white px-7 py-[13px] text-[15px] font-semibold text-ink transition-colors hover:border-ink"
        >
          홈으로
        </Link>
      </div>
    </div>
  );
}

function Summary({
  label,
  value,
  tone = "default",
}: {
  label: string;
  value: string;
  tone?: "default" | "success" | "alert";
}) {
  const valueClass =
    tone === "success" ? "text-success" : tone === "alert" ? "text-alert" : "text-ink";
  return (
    <div className="flex flex-col items-center gap-1 bg-white px-4 py-5">
      <span className="text-[12px] font-semibold text-muted">{label}</span>
      <span className={`text-[20px] font-bold tabular-nums ${valueClass}`}>{value}</span>
    </div>
  );
}

function Section({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="rounded-[12px] border border-line-card bg-subtle px-4 py-3.5">
      <div className="mb-1.5 text-[11.5px] font-bold text-secondary">{label}</div>
      <div className="text-[14px] leading-[1.65] text-body">{children}</div>
    </div>
  );
}
