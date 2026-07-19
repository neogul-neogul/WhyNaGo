"use client";

import { useMemo, useState } from "react";
import type { EssayQuestion } from "@/types";
import { diffBg, diffColor, lvBadge } from "@/lib/badges";

type Entry = { label: string; question: string; answer: string; feedback: string; model: string };

// 서술형 풀이 (AI 면접식 꼬리질문)
export default function EssayQuiz({
  question,
  onQuit,
}: {
  question: EssayQuestion;
  onQuit: () => void;
}) {
  const seq = useMemo(() => [question.text, ...question.followups], [question]);
  const models = useMemo(() => [question.model, ...question.followupModels], [question]);

  const [thread, setThread] = useState<Entry[]>([]);
  const [draft, setDraft] = useState("");
  const [open, setOpen] = useState<number[]>([]);

  const idx = thread.length;
  const active = idx < seq.length;
  const done = idx >= seq.length;

  const submit = () => {
    if (!draft.trim()) return;
    const entry: Entry = {
      label: idx === 0 ? "본 질문" : `꼬리질문 ${idx}`,
      question: seq[idx],
      answer: draft.trim(),
      feedback: question.feedbacks[idx] ?? "",
      model: models[idx] ?? "",
    };
    setThread((t) => [...t, entry]);
    setDraft("");
  };

  const toggle = (i: number) =>
    setOpen((o) => (o.includes(i) ? o.filter((x) => x !== i) : [...o, i]));

  const askedUpto = done ? seq.length : idx + 1;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3.5">
        <button
          type="button"
          onClick={onQuit}
          className="flex items-center gap-1.5 text-[13px] font-semibold text-[#6B6B62]"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M11 18l-6-6 6-6" />
          </svg>
          돌아가기
        </button>
      </div>

      <div className="flex items-start gap-[18px]">
        {/* LEFT : 질문 목록 */}
        <div className="min-w-0 flex-1 overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
          <div className="flex items-center gap-2.5 border-b border-[#ECECE8] bg-[#FAFAF7] px-[22px] py-[13px]">
            <span className="rounded-[6px] bg-[#F0EDFF] px-2.5 py-[3px] text-xs font-bold text-[#6D28D9]">서술형</span>
            <span
              className="rounded-[6px] px-[9px] py-[3px] text-xs font-semibold"
              style={{ color: diffColor(question.diff), background: diffBg(question.diff) }}
            >
              난이도 {lvBadge(question.diff)}
            </span>
            <span className="ml-auto rounded-[6px] bg-[#F1F1ED] px-2.5 py-[3px] text-xs font-semibold text-[#6B6B62]">
              {question.cat}
            </span>
          </div>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {seq.slice(0, askedUpto).map((text, i) => {
              const isActive = !done && i === idx;
              return (
                <div
                  key={i}
                  className="flex flex-col gap-2 rounded-[12px] px-4 py-3.5"
                  style={{
                    border: `1px solid ${isActive ? "#C7CCFF" : "#ECECE8"}`,
                    background: isActive ? "#F5F6FF" : "#fff",
                  }}
                >
                  <span className="inline-flex w-fit items-center rounded-[5px] bg-[#EEF0FF] px-[9px] py-0.5 text-[11px] font-bold text-[#4F46E5]">
                    {i === 0 ? "본 질문" : `꼬리질문 ${i}`}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-[#1C1C1A]">{text}</div>
                </div>
              );
            })}
          </div>
        </div>

        {/* RIGHT : 답변 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <div className="overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
            <div className="flex items-center gap-2 border-b border-[#ECECE8] bg-[#FAFAF7] px-[22px] py-[13px]">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-[5px] bg-[#1C1C1A] text-[10px] font-bold text-white">AI</span>
              <span className="text-[13px] font-bold text-[#6B6B62]">AI 면접관에게 답하듯 작성하세요</span>
            </div>
            <div className="flex flex-col gap-3.5 px-[22px] py-5">
              {/* 답변 완료 기록 */}
              {thread.map((e, i) => (
                <div key={i} className="overflow-hidden rounded-[12px] border border-[#ECECE8]">
                  <div className="flex items-center gap-1.5 border-b border-[#F2F2EE] bg-[#FAFAF7] px-4 py-[9px]">
                    <span className="inline-flex items-center rounded-[5px] bg-[#EBEBE6] px-2 py-0.5 text-[10.5px] font-bold text-[#6B6B62]">
                      {e.label} · 내 답변
                    </span>
                  </div>
                  <div className="whitespace-pre-wrap px-4 py-[13px] text-[14px] leading-[1.65] text-[#3A3A34]">
                    {e.answer}
                  </div>
                  <button
                    type="button"
                    onClick={() => toggle(i)}
                    className="flex w-full items-center justify-between gap-2 border-t border-[#F2F2EE] bg-white px-4 py-2.5 text-[12.5px] font-semibold text-[#4F46E5]"
                  >
                    <span className="flex items-center gap-1.5">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M12 2a10 10 0 100 20 10 10 0 000-20z" />
                        <path d="M12 16v-4M12 8h.01" />
                      </svg>
                      {open.includes(i) ? "해설·피드백 숨기기" : "해설·피드백 보기"}
                    </span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                      <path d={open.includes(i) ? "M18 15l-6-6-6 6" : "M6 9l6 6 6-6"} />
                    </svg>
                  </button>
                  {open.includes(i) && (
                    <div className="flex flex-col gap-3 border-t border-[#F2F2EE] bg-[#FCFCFA] px-4 py-3.5">
                      <div>
                        <div className="mb-1.5 text-[11.5px] font-bold text-[#C2410C]">AI 피드백</div>
                        <div className="text-[13.5px] leading-[1.65] text-[#7A4A38]">{e.feedback}</div>
                      </div>
                      <div className="rounded-[10px] bg-[#F2FBF5] px-[15px] py-[13px]">
                        <div className="mb-1.5 text-[11.5px] font-bold text-[#16A34A]">모범답안 · 해설</div>
                        <div className="text-[13.5px] leading-[1.7] text-[#3A3A34]">{e.model}</div>
                      </div>
                    </div>
                  )}
                </div>
              ))}

              {/* 현재 질문 답변 입력 */}
              {active && (
                <div className="flex flex-col gap-[11px]">
                  <div className="flex items-center gap-1.5">
                    <span className="inline-flex items-center rounded-[5px] bg-[#EEF0FF] px-[9px] py-0.5 text-[11px] font-bold text-[#4F46E5]">
                      {idx === 0 ? "본 질문" : `꼬리질문 ${idx}`}
                    </span>
                    <span className="text-xs font-semibold text-[#9A9A90]">에 답변해 주세요</span>
                  </div>
                  <textarea
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                    placeholder="면접관에게 답하듯 설명해 보세요. 근거와 예시를 함께 들면 좋습니다."
                    className="block min-h-[170px] w-full resize-y rounded-[12px] border border-[#E0E0DA] bg-white px-4 py-3.5 text-[14.5px] leading-[1.7] text-[#1C1C1A] outline-none"
                  />
                  <span className="font-mono text-xs text-[#A8A8A0]">{draft.length}자</span>
                </div>
              )}

              {/* 면접 완료 */}
              {done && (
                <div className="flex flex-col gap-2 border-t border-dashed border-[#E6E6E0] pt-4">
                  <div className="flex items-center gap-1.5 text-[15px] font-bold text-[#16A34A]">
                    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 6L9 17l-5-5" />
                    </svg>
                    면접 완료 · 꼬리질문까지 모두 답했어요
                  </div>
                  <div className="text-[13px] text-[#9A9A90]">
                    각 답변의 <b>해설·피드백 보기</b>를 눌러 모범답안과 비교해 보세요.
                  </div>
                </div>
              )}
            </div>
            <div className="flex items-center justify-end gap-2 border-t border-[#ECECE8] px-[22px] py-3.5">
              {done && (
                <span className="mr-auto text-[13.5px] font-bold text-[#16A34A]">정답입니다</span>
              )}
              {active && (
                <button
                  type="button"
                  onClick={submit}
                  disabled={!draft.trim()}
                  className="rounded-[10px] px-[24px] py-[11px] text-[14px] font-semibold text-white"
                  style={{ background: !draft.trim() ? "#C8C8C0" : "#1C1C1A", cursor: !draft.trim() ? "not-allowed" : "pointer" }}
                >
                  {idx < seq.length - 1 ? "답변 제출" : "마지막 답변 제출"}
                </button>
              )}
              {done && (
                /* 면접 완료: 저장하고 문제은행으로 복귀 (디자인: 저장하기 → setup) */
                <button
                  type="button"
                  onClick={onQuit}
                  className="rounded-[10px] bg-[#1C1C1A] px-6 py-[11px] text-[14px] font-semibold text-white"
                >
                  저장하기
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
