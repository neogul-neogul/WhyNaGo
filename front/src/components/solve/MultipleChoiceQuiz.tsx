"use client";

import { useMemo, useState } from "react";
import type { MultipleChoiceQuestion } from "@/types";
import { diffBg, diffColor, lvBadge } from "@/lib/badges";

type Cell = { selected: number | null; checked: boolean };

// 객관식 풀이 (프로그래머스식 좌우 분할 + 꼬리질문 탭)
export default function MultipleChoiceQuiz({
  question,
  onQuit,
  onFinish,
}: {
  question: MultipleChoiceQuestion;
  onQuit: () => void;
  onFinish: (correct: number, total: number) => void;
}) {
  // 본 질문 + 꼬리질문 시퀀스
  const seq = useMemo(
    () => [question, ...question.followups],
    [question],
  );
  const [state, setState] = useState<Cell[]>(() => seq.map(() => ({ selected: null, checked: false })));
  const [revealed, setRevealed] = useState(1);
  const [tab, setTab] = useState(0);
  const [correct, setCorrect] = useState(0);

  const mtab = Math.min(tab, seq.length - 1);
  const cell = state[mtab];
  const aq = seq[mtab];
  const allAnswered = revealed >= seq.length && state.every((x) => x.checked);

  const select = (i: number) => {
    if (cell.checked) return;
    setState((st) => st.map((x, idx) => (idx === mtab ? { ...x, selected: i } : x)));
  };

  const check = () => {
    if (cell.selected === null || cell.checked) return;
    const ok = cell.selected === aq.answer;
    setState((st) => st.map((x, idx) => (idx === mtab ? { ...x, checked: true } : x)));
    if (mtab === revealed - 1 && revealed < seq.length) setRevealed((r) => r + 1);
    if (ok) setCorrect((c) => c + 1);
  };

  const goTab = (i: number) => {
    if (i < revealed) setTab(i);
  };
  const nextUnanswered = () => {
    const next = state.findIndex((x, idx) => idx < revealed && !x.checked);
    if (next >= 0) setTab(next);
  };

  const ansOk = cell.checked && cell.selected === aq.answer;
  const wrongPicked = cell.checked && cell.selected !== null && cell.selected !== aq.answer;

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
        {/* LEFT : 문제 설명 */}
        <div className="min-w-0 flex-1 overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
          <div className="flex items-center gap-2.5 border-b border-[#ECECE8] bg-[#FAFAF7] px-[22px] py-[13px]">
            <span className="rounded-[6px] bg-[#EEF0FF] px-2.5 py-[3px] text-xs font-bold text-[#4F46E5]">객관식</span>
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
            {seq.slice(0, revealed).map((qq, i) => {
              const active = i === mtab;
              const done = state[i]?.checked;
              return (
                <div
                  key={i}
                  onClick={() => goTab(i)}
                  className="flex cursor-pointer flex-col gap-2 rounded-[12px] px-4 py-3.5"
                  style={{
                    border: `1px solid ${active ? "#C7CCFF" : "#ECECE8"}`,
                    background: active ? "#F5F6FF" : "#fff",
                  }}
                >
                  <span
                    className="inline-flex w-fit items-center rounded-[5px] px-[9px] py-0.5 text-[11px] font-bold"
                    style={{
                      color: done ? "#16A34A" : "#4F46E5",
                      background: done ? "#EAF7EF" : "#EEF0FF",
                    }}
                  >
                    {i === 0 ? "본 질문" : `꼬리질문 ${i}`}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-[#1C1C1A]">
                    {qq.text}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* RIGHT : 답안 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <div className="overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
            {/* 탭 */}
            <div className="flex items-center gap-0 overflow-x-auto border-b border-[#ECECE8] bg-[#FAFAF7] px-2">
              {seq.slice(0, revealed).map((qq, i) => {
                const active = i === mtab;
                const done = state[i]?.checked;
                const ok = done && state[i].selected === qq.answer;
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => goTab(i)}
                    className="flex items-center gap-1.5 px-4 py-3 text-[13px] transition-all"
                    style={{
                      fontWeight: active ? 700 : 600,
                      color: active ? "#1C1C1A" : "#9A9A90",
                      borderBottom: `2px solid ${active ? "#1C1C1A" : "transparent"}`,
                    }}
                  >
                    {i === 0 ? "본 질문" : `꼬리 ${i}`}
                    <span style={{ fontWeight: 700, color: ok ? "#16A34A" : "#DC2626" }}>
                      {done ? (ok ? "✓" : "✕") : ""}
                    </span>
                  </button>
                );
              })}
            </div>

            <div className="flex items-center gap-2 px-[22px] pb-1.5 pt-[18px]">
              <span className="inline-flex items-center rounded-[5px] bg-[#EEF0FF] px-[9px] py-0.5 text-[11px] font-bold text-[#4F46E5]">
                {mtab === 0 ? "본 질문" : `꼬리질문 ${mtab}`}
              </span>
              <span className="text-xs font-semibold text-[#9A9A90]">
                {mtab === 0
                  ? "개념을 묻는 본 질문입니다"
                  : `앞선 답변에서 파생된 꼬리질문 ${mtab}/${seq.length - 1}`}
              </span>
            </div>

            {/* 선택지 */}
            <div className="flex flex-col gap-2.5 px-[22px] pb-5 pt-3.5">
              {aq.options.map((opt, i) => {
                const sel = cell.selected === i;
                const isAns = i === aq.answer;
                let border = "#E6E6E0", bg = "#fff", numBg = "#F1F1ED", numColor = "#8A8A80", mark = "", markColor = "transparent";
                if (cell.checked) {
                  if (isAns) { border = "#16A34A"; bg = "#F2FBF5"; numBg = "#16A34A"; numColor = "#fff"; mark = "정답"; markColor = "#16A34A"; }
                  else if (sel) { border = "#DC2626"; bg = "#FEF2F2"; numBg = "#DC2626"; numColor = "#fff"; mark = "오답"; markColor = "#DC2626"; }
                } else if (sel) { border = "#4F46E5"; bg = "#F5F5FF"; numBg = "#4F46E5"; numColor = "#fff"; }
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => select(i)}
                    className="flex w-full items-center gap-[13px] rounded-[12px] px-[17px] py-[15px] text-left transition-all"
                    style={{ border: `1.5px solid ${border}`, background: bg, cursor: cell.checked ? "default" : "pointer" }}
                  >
                    <span
                      className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full text-[12.5px] font-bold"
                      style={{ background: numBg, color: numColor }}
                    >
                      {i + 1}
                    </span>
                    <span className="flex-1 text-[14.5px] leading-[1.5]">{opt}</span>
                    <span className="text-[13px] font-bold" style={{ color: markColor }}>{mark}</span>
                  </button>
                );
              })}

              {cell.checked && (
                <div className="mt-1.5 flex flex-col gap-3 border-t border-dashed border-[#E6E6E0] pt-4">
                  <div className="text-[15px] font-bold" style={{ color: ansOk ? "#16A34A" : "#DC2626" }}>
                    {ansOk ? "✓ 정답입니다" : "✕ 오답입니다 · 오답노트에 자동 저장됨"}
                  </div>
                  <div className="rounded-[12px] bg-[#FAFAF7] px-[18px] py-4">
                    <div className="mb-[7px] text-xs font-semibold text-[#8A8A80]">정답 해설</div>
                    <div className="text-[14px] leading-[1.65] text-[#3A3A34]">{aq.explanation}</div>
                  </div>
                  {wrongPicked && aq.optExp?.[cell.selected as number] && (
                    <div className="rounded-[12px] border border-[#F6DAD3] bg-[#FEF4F2] px-[18px] py-4">
                      <div className="mb-[7px] text-xs font-semibold text-[#C2410C]">
                        내가 고른 답 — {(cell.selected as number) + 1}번 · 왜 틀렸나
                      </div>
                      <div className="text-[14px] leading-[1.65] text-[#7A342A]">
                        {aq.optExp[cell.selected as number]}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 푸터 */}
            <div className="flex justify-end gap-2 border-t border-[#ECECE8] px-[22px] py-3.5">
              {/* 모든 질문을 다 풀면 종료하기 버튼을 숨긴다 (디자인: showEndQuiz) */}
              {!allAnswered && (
                <button
                  type="button"
                  onClick={() => onFinish(correct, seq.length)}
                  className="rounded-[10px] border border-[#DCDCD4] bg-white px-[22px] py-[11px] text-[14px] font-semibold text-[#6B6B62]"
                >
                  종료하기
                </button>
              )}
              {!cell.checked ? (
                <button
                  type="button"
                  onClick={check}
                  disabled={cell.selected === null}
                  className="rounded-[10px] px-[26px] py-[11px] text-[14px] font-semibold text-white"
                  style={{ background: cell.selected === null ? "#C8C8C0" : "#1C1C1A", cursor: cell.selected === null ? "not-allowed" : "pointer" }}
                >
                  정답 확인
                </button>
              ) : !allAnswered ? (
                <button
                  type="button"
                  onClick={nextUnanswered}
                  className="rounded-[10px] bg-[#1C1C1A] px-[26px] py-[11px] text-[14px] font-semibold text-white"
                >
                  다음 질문
                </button>
              ) : (
                /* 다 풀었을 때: 저장하고 문제은행으로 복귀 (디자인: 저장하기 → setup) */
                <button
                  type="button"
                  onClick={onQuit}
                  className="rounded-[10px] bg-[#1C1C1A] px-[26px] py-[11px] text-[14px] font-semibold text-white"
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
