"use client";

import { useState } from "react";
import type { WrongNote } from "@/types";
import { diffBg, diffColor } from "@/lib/badges";

// 오답 상세 (문제 풀이 꼬리질문 완료 화면과 동일 형태, 모두 채점된 상태)
export default function WrongDetail({
  note,
  bookmarked,
  onToggleBookmark,
  onBack,
}: {
  note: WrongNote;
  bookmarked: boolean;
  onToggleBookmark: () => void;
  onBack: () => void;
}) {
  const seq = [
    {
      text: note.q,
      options: note.options,
      answer: note.correctAnswer,
      myAnswer: note.myAnswer,
      explanation: note.explanation,
      wrongExp: note.wrongExp,
    },
    ...note.followups,
  ];
  const [tab, setTab] = useState(0);
  const wtab = Math.min(tab, seq.length - 1);
  const aq = seq[wtab];
  const aqWrong = aq.myAnswer !== aq.answer;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3.5">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 text-[13px] font-semibold text-[#6B6B62]"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M11 18l-6-6 6-6" />
          </svg>
          돌아가기
        </button>
      </div>

      <div className="flex items-start gap-[18px]">
        {/* LEFT : 문제 설명 */}
        <div className="min-w-0 flex-1 overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
          <div className="flex items-center gap-2.5 border-b border-[#ECECE8] bg-[#FAFAF7] px-[22px] py-[13px]">
            <span className="rounded-[6px] bg-[#EEF0FF] px-2.5 py-[3px] text-xs font-bold text-[#4F46E5]">객관식</span>
            <span
              className="rounded-[6px] px-[9px] py-[3px] text-xs font-semibold"
              style={{ color: diffColor(note.diff), background: diffBg(note.diff) }}
            >
              난이도 {note.diff}
            </span>
            <span className="ml-auto rounded-[6px] bg-[#F1F1ED] px-2.5 py-[3px] text-xs font-semibold text-[#6B6B62]">
              {note.cat}
            </span>
          </div>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {seq.map((qq, i) => {
              const active = i === wtab;
              const done = qq.myAnswer === qq.answer;
              return (
                <div
                  key={i}
                  onClick={() => setTab(i)}
                  className="flex cursor-pointer flex-col gap-2 rounded-[12px] px-4 py-3.5"
                  style={{
                    border: `1px solid ${active ? "#C7CCFF" : "#ECECE8"}`,
                    background: active ? "#F5F6FF" : "#fff",
                  }}
                >
                  <span
                    className="inline-flex w-fit items-center rounded-[5px] px-[9px] py-0.5 text-[11px] font-bold"
                    style={{
                      color: done ? "#16A34A" : "#DC2626",
                      background: done ? "#EAF7EF" : "#FEECEC",
                    }}
                  >
                    {i === 0 ? "본 질문" : `꼬리질문 ${i}`}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-[#1C1C1A]">{qq.text}</div>
                </div>
              );
            })}
            <div className="text-xs text-[#A8A8A0]">
              {note.source} · {note.solvedAt}
            </div>
          </div>
        </div>

        {/* RIGHT : 답안 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <div className="overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
            <div className="flex items-center gap-0 overflow-x-auto border-b border-[#ECECE8] bg-[#FAFAF7] px-2.5">
              {seq.map((qq, i) => {
                const active = i === wtab;
                const done = qq.myAnswer === qq.answer;
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => setTab(i)}
                    className="flex items-center gap-1.5 px-4 py-3 text-[13px] transition-all"
                    style={{
                      fontWeight: active ? 700 : 600,
                      color: active ? "#1C1C1A" : "#9A9A90",
                      borderBottom: `2px solid ${active ? "#1C1C1A" : "transparent"}`,
                    }}
                  >
                    {i === 0 ? "본 질문" : `꼬리 ${i}`}
                    <span style={{ fontWeight: 700, color: done ? "#16A34A" : "#DC2626" }}>
                      {done ? "✓" : "✕"}
                    </span>
                  </button>
                );
              })}
              <button
                type="button"
                onClick={onToggleBookmark}
                title="북마크"
                className="ml-auto flex p-0.5"
                style={{ color: bookmarked ? "#4F46E5" : "#C4C4BC" }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill={bookmarked ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
                </svg>
              </button>
            </div>

            <div className="flex items-center gap-2 px-[22px] pb-1.5 pt-[18px]">
              <span className="inline-flex items-center rounded-[5px] bg-[#EEF0FF] px-[9px] py-0.5 text-[11px] font-bold text-[#4F46E5]">
                {wtab === 0 ? "본 질문" : `꼬리질문 ${wtab}`}
              </span>
              <span className="text-xs font-semibold text-[#9A9A90]">
                {wtab === 0
                  ? "개념을 묻는 본 질문입니다"
                  : `앞선 답변에서 파생된 꼬리질문 ${wtab}/${seq.length - 1}`}
              </span>
            </div>

            <div className="flex flex-col gap-2.5 px-[22px] pb-5 pt-3.5">
              {aq.options.map((opt, oi) => {
                const sel = oi === aq.myAnswer;
                const isAns = oi === aq.answer;
                let border = "#E6E6E0", bg = "#fff", numBg = "#F1F1ED", numColor = "#8A8A80", mark = "", markColor = "transparent";
                if (isAns) { border = "#16A34A"; bg = "#F2FBF5"; numBg = "#16A34A"; numColor = "#fff"; mark = "정답"; markColor = "#16A34A"; }
                else if (sel) { border = "#DC2626"; bg = "#FEF2F2"; numBg = "#DC2626"; numColor = "#fff"; mark = "오답"; markColor = "#DC2626"; }
                return (
                  <div
                    key={oi}
                    className="flex w-full items-center gap-[13px] rounded-[12px] px-[17px] py-[15px]"
                    style={{ border: `1.5px solid ${border}`, background: bg }}
                  >
                    <span
                      className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full text-[12.5px] font-bold"
                      style={{ background: numBg, color: numColor }}
                    >
                      {oi + 1}
                    </span>
                    <span className="flex-1 text-[14.5px] leading-[1.5]">{opt}</span>
                    <span className="text-[13px] font-bold" style={{ color: markColor }}>{mark}</span>
                  </div>
                );
              })}
              <div className="mt-1.5 flex flex-col gap-3 border-t border-dashed border-[#E6E6E0] pt-4">
                <div className="text-[15px] font-bold" style={{ color: aqWrong ? "#DC2626" : "#16A34A" }}>
                  {aqWrong ? "✕ 오답입니다 · 오답노트에 자동 저장됨" : "✓ 정답입니다"}
                </div>
                <div className="rounded-[12px] bg-[#FAFAF7] px-[18px] py-4">
                  <div className="mb-[7px] text-xs font-semibold text-[#8A8A80]">정답 해설</div>
                  <div className="text-[14px] leading-[1.65] text-[#3A3A34]">{aq.explanation}</div>
                </div>
                {aqWrong && aq.wrongExp && (
                  <div className="rounded-[12px] border border-[#F6DAD3] bg-[#FEF4F2] px-[18px] py-4">
                    <div className="mb-[7px] text-xs font-semibold text-[#C2410C]">
                      내가 고른 답 — {aq.myAnswer + 1}번 · 왜 틀렸나
                    </div>
                    <div className="text-[14px] leading-[1.65] text-[#7A342A]">{aq.wrongExp}</div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
