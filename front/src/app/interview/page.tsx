"use client";

import { useState } from "react";
import { interviewBank } from "@/mocks/interview";
import { CATEGORIES } from "@/lib/badges";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";

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
          <div className="flex items-center gap-[11px] rounded-[13px] border border-[#E0D8FF] bg-[#F0EDFF] px-[18px] py-3.5">
            <span className="flex-shrink-0 rounded-[6px] bg-[#6D28D9] px-[9px] py-1 text-[11px] font-bold text-white">AI</span>
            <span className="text-[13px] leading-[1.5] text-[#5B21B6]">
              AI는 면접 질문과 피드백에만 사용됩니다. 문제 추천·오답 분석·학습 계획에는 사용하지 않아요. <b>하루 1회</b> 진행할 수 있습니다.
            </span>
          </div>

          {/* intro */}
          {stage === "intro" && (
            <div className="flex flex-col gap-[22px] rounded-[16px] border border-[#ECECE8] bg-white p-7">
              <div>
                <div className="mb-[11px] text-[13px] font-semibold text-[#8A8A80]">면접 카테고리</div>
                <div className="flex flex-wrap gap-2">
                  {CATEGORIES.map((c) => (
                    <Chip key={c} label={c} active={cat === c} onClick={() => setCat(c)} />
                  ))}
                </div>
              </div>
              <div className="flex flex-col gap-1.5 rounded-[12px] bg-[#FAFAF7] px-5 py-[18px]">
                <span className="text-xs font-semibold text-[#8A8A80]">오늘의 면접 진행 순서</span>
                <span className="text-[13.5px] leading-[1.7] text-[#3A3A34]">
                  질문 1개 제공 → 답변 입력 → AI 피드백 → 꼬리 질문 1개 → 개선 답변 예시 → 결과 저장
                </span>
              </div>
              <button
                type="button"
                onClick={() => {
                  setAnswer("");
                  setStage("answering");
                }}
                className="flex items-center gap-2 self-start rounded-[11px] bg-[#6D28D9] px-7 py-[13px] text-[15px] font-semibold text-white"
              >
                면접 시작
                <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
                  <path d="M5 12h14M13 6l6 6-6 6" />
                </svg>
              </button>
            </div>
          )}

          {/* answering */}
          {stage === "answering" && (
            <div className="flex flex-col gap-[18px] rounded-[16px] border border-[#ECECE8] bg-white p-7">
              <div className="flex items-center gap-2">
                <span className="rounded-[6px] bg-[#F0EDFF] px-2.5 py-1 text-xs font-semibold text-[#6D28D9]">{cat}</span>
                <span className="text-xs text-[#A8A8A0]">질문 1 / 1</span>
              </div>
              <div className="flex items-start gap-[13px]">
                <div className="flex h-[34px] w-[34px] flex-shrink-0 items-center justify-center rounded-[9px] bg-[#6D28D9] text-xs font-bold text-white">AI</div>
                <div className="pt-1 text-[18px] font-semibold leading-[1.55] tracking-[-0.2px]">{iv.q}</div>
              </div>
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder="답변을 입력하세요. 핵심 개념과 근거를 함께 설명하면 더 정확한 피드백을 받을 수 있어요."
                className="min-h-[150px] w-full resize-y rounded-[12px] border border-[#E0E0DA] bg-[#FAFAF7] p-4 text-[14.5px] leading-[1.65] text-[#1C1C1A] outline-none"
              />
              <button
                type="button"
                onClick={() => setStage("feedback")}
                className="self-end rounded-[11px] bg-[#6D28D9] px-[26px] py-3 text-[14.5px] font-semibold text-white"
              >
                답변 제출 · AI 피드백 받기
              </button>
            </div>
          )}

          {/* feedback */}
          {stage === "feedback" && (
            <div className="flex flex-col gap-3.5">
              <div className="flex flex-col gap-2 rounded-[16px] border border-[#ECECE8] bg-white px-[26px] py-6">
                <span className="self-start rounded-[6px] bg-[#F0EDFF] px-2.5 py-1 text-xs font-semibold text-[#6D28D9]">{cat}</span>
                <div className="mt-1 text-xs text-[#8A8A80]">질문</div>
                <div className="text-[16px] font-semibold leading-[1.55]">{iv.q}</div>
                <div className="mt-2 text-xs text-[#8A8A80]">내 답변</div>
                <div className="rounded-[10px] bg-[#FAFAF7] p-3.5 text-[14px] leading-[1.6] text-[#5A5A52]">
                  {answer.trim() ? answer : "(답변 미입력)"}
                </div>
              </div>

              <div className="flex flex-col gap-[13px] rounded-[16px] border border-[#ECECE8] bg-white px-[26px] py-6">
                <div className="flex items-center gap-2.5">
                  <div className="flex h-7 w-7 items-center justify-center rounded-[8px] bg-[#6D28D9] text-[11px] font-bold text-white">AI</div>
                  <span className="text-[15px] font-bold">피드백</span>
                </div>
                <div className="text-[14.5px] leading-[1.7] text-[#3A3A34]">{iv.feedback}</div>
              </div>

              <div className="flex flex-col gap-2.5 rounded-[16px] border border-[#E0D8FF] bg-[#F0EDFF] px-[26px] py-6">
                <div className="text-[13px] font-bold text-[#5B21B6]">꼬리 질문</div>
                <div className="text-[15.5px] font-semibold leading-[1.55] text-[#1C1C1A]">{iv.followup}</div>
              </div>

              <div className="flex flex-col gap-[11px] rounded-[16px] border border-[#ECECE8] bg-white px-[26px] py-6">
                <div className="text-[13px] font-bold text-[#16A34A]">개선 답변 예시</div>
                <div className="rounded-[10px] bg-[#F2FBF5] p-4 text-[14.5px] leading-[1.75] text-[#3A3A34]">{iv.improved}</div>
                <div className="mt-1 flex flex-wrap items-center gap-1.5">
                  <span className="text-xs text-[#A8A8A0]">관련 키워드</span>
                  {iv.keywords.map((kw) => (
                    <span key={kw} className="rounded-[20px] bg-[#F1F1ED] px-2.5 py-1 text-xs font-medium text-[#6B6B62]">{kw}</span>
                  ))}
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => {
                    setStage("intro");
                    setAnswer("");
                  }}
                  className="flex-1 rounded-[11px] border border-[#DCDCD4] bg-white p-[13px] text-[14.5px] font-semibold text-[#1C1C1A]"
                >
                  처음으로
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setStage("intro");
                    setAnswer("");
                  }}
                  className="flex-1 rounded-[11px] bg-[#16A34A] p-[13px] text-[14.5px] font-semibold text-white"
                >
                  면접 결과 저장
                </button>
              </div>
            </div>
          )}
        </div>
      </PageBody>
    </main>
  );
}
