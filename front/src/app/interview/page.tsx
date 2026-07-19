"use client";

import { useState } from "react";
import { interviewBank } from "@/mocks/interview";
import { CATEGORIES } from "@/lib/badges";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

type Stage = "intro" | "answering" | "feedback";

export default function InterviewPage() {
  const [stage, setStage] = useState<Stage>("intro");
  const [cat, setCat] = useState("네트워크");
  const [answer, setAnswer] = useState("");

  const iv = interviewBank[cat] ?? interviewBank["전체"];

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="1일 1면접" subtitle="AI와 함께하는 하루 한 번의 모의 면접" />
      <PageBody>
        <div className="flex max-w-[780px] flex-col gap-[18px]">
          {/* AI 안내 배너 */}
          <div className="flex items-center gap-[11px] rounded-[13px] border border-ai-line bg-ai-bg px-[18px] py-3.5">
            <span className="flex-shrink-0 rounded-[6px] bg-ai px-[9px] py-1 text-[11px] font-bold text-white">AI</span>
            <span className="text-[13px] leading-[1.5] text-ai-deep">
              AI는 면접 질문과 피드백에만 사용됩니다. 문제 추천·오답 분석·학습 계획에는 사용하지 않아요. <b>하루 1회</b> 진행할 수 있습니다.
            </span>
          </div>

          {/* intro */}
          {stage === "intro" && (
            <Card className="flex flex-col gap-[22px] p-7">
              <div>
                <div className="mb-[11px] text-[13px] font-semibold text-muted">면접 카테고리</div>
                <div className="flex flex-wrap gap-2">
                  {CATEGORIES.map((c) => (
                    <Chip key={c} label={c} active={cat === c} onClick={() => setCat(c)} />
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
                onClick={() => {
                  setAnswer("");
                  setStage("answering");
                }}
                className="flex items-center gap-2 self-start"
              >
                면접 시작
                <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
                  <path d="M5 12h14M13 6l6 6-6 6" />
                </svg>
              </Button>
            </Card>
          )}

          {/* answering */}
          {stage === "answering" && (
            <Card className="flex flex-col gap-[18px] p-7">
              <div className="flex items-center gap-2">
                <Badge tone="ai">{cat}</Badge>
                <span className="text-xs text-placeholder">질문 1 / 1</span>
              </div>
              <div className="flex items-start gap-[13px]">
                <div className="flex h-[34px] w-[34px] flex-shrink-0 items-center justify-center rounded-[9px] bg-ai text-xs font-bold text-white">AI</div>
                <div className="pt-1 text-[18px] font-semibold leading-[1.55] tracking-[-0.2px]">{iv.q}</div>
              </div>
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder="답변을 입력하세요. 핵심 개념과 근거를 함께 설명하면 더 정확한 피드백을 받을 수 있어요."
                className="min-h-[150px] w-full resize-y rounded-[12px] border border-line-input bg-subtle p-4 text-[14.5px] leading-[1.65] text-ink outline-none"
              />
              <Button variant="ai" size="lg" onClick={() => setStage("feedback")} className="self-end">
                답변 제출 · AI 피드백 받기
              </Button>
            </Card>
          )}

          {/* feedback */}
          {stage === "feedback" && (
            <div className="flex flex-col gap-3.5">
              <Card className="flex flex-col gap-2 px-[26px] py-6">
                <Badge tone="ai" className="self-start">{cat}</Badge>
                <div className="mt-1 text-xs text-muted">질문</div>
                <div className="text-[16px] font-semibold leading-[1.55]">{iv.q}</div>
                <div className="mt-2 text-xs text-muted">내 답변</div>
                <div className="rounded-[10px] bg-subtle p-3.5 text-[14px] leading-[1.6] text-dim">
                  {answer.trim() ? answer : "(답변 미입력)"}
                </div>
              </Card>

              <Card className="flex flex-col gap-[13px] px-[26px] py-6">
                <div className="flex items-center gap-2.5">
                  <div className="flex h-7 w-7 items-center justify-center rounded-[8px] bg-ai text-[11px] font-bold text-white">AI</div>
                  <span className="text-[15px] font-bold">피드백</span>
                </div>
                <div className="text-[14.5px] leading-[1.7] text-body">{iv.feedback}</div>
              </Card>

              <div className="flex flex-col gap-2.5 rounded-[16px] border border-ai-line bg-ai-bg px-[26px] py-6">
                <div className="text-[13px] font-bold text-ai-deep">꼬리 질문</div>
                <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">{iv.followup}</div>
              </div>

              <Card className="flex flex-col gap-[11px] px-[26px] py-6">
                <div className="text-[13px] font-bold text-success">개선 답변 예시</div>
                <div className="rounded-[10px] bg-success-bg p-4 text-[14.5px] leading-[1.75] text-body">{iv.improved}</div>
                <div className="mt-1 flex flex-wrap items-center gap-1.5">
                  <span className="text-xs text-placeholder">관련 키워드</span>
                  {iv.keywords.map((kw) => (
                    <span key={kw} className="rounded-[20px] bg-neutral px-2.5 py-1 text-xs font-medium text-secondary">{kw}</span>
                  ))}
                </div>
              </Card>

              <div className="flex gap-3">
                <Button
                  variant="secondary"
                  size="xl"
                  onClick={() => {
                    setStage("intro");
                    setAnswer("");
                  }}
                  className="flex-1"
                >
                  처음으로
                </Button>
                <Button
                  variant="success"
                  size="xl"
                  onClick={() => {
                    setStage("intro");
                    setAnswer("");
                  }}
                  className="flex-1"
                >
                  면접 결과 저장
                </Button>
              </div>
            </div>
          )}
        </div>
      </PageBody>
    </main>
  );
}
