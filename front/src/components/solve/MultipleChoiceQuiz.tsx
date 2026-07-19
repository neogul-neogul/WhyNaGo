"use client";

import { useMemo, useState } from "react";
import type { MultipleChoiceQuestion } from "@/types";
import { diffTone, lvBadge } from "@/lib/badges";
import { palette } from "@/lib/tokens";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card, { CardHeader } from "@/components/ui/Card";

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
          className="flex items-center gap-1.5 text-[13px] font-semibold text-secondary"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M11 18l-6-6 6-6" />
          </svg>
          돌아가기
        </button>
      </div>

      <div className="flex items-start gap-[18px]">
        {/* LEFT : 문제 설명 */}
        <Card className="min-w-0 flex-1 overflow-hidden">
          <CardHeader className="gap-2.5">
            <Badge tone="accent">객관식</Badge>
            <Badge tone={diffTone(question.diff)}>난이도 {lvBadge(question.diff)}</Badge>
            <Badge tone="neutral" className="ml-auto">{question.cat}</Badge>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {seq.slice(0, revealed).map((qq, i) => {
              const active = i === mtab;
              const done = state[i]?.checked;
              return (
                <div
                  key={i}
                  onClick={() => goTab(i)}
                  className={`flex cursor-pointer flex-col gap-2 rounded-[12px] border px-4 py-3.5 ${
                    active ? "border-accent-line bg-accent-faint" : "border-line-card bg-white"
                  }`}
                >
                  <span
                    className={`inline-flex w-fit items-center rounded-[5px] px-[9px] py-0.5 text-[11px] font-bold ${
                      done ? "bg-success-bg text-success" : "bg-accent-bg text-accent"
                    }`}
                  >
                    {i === 0 ? "본 질문" : `꼬리질문 ${i}`}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">
                    {qq.text}
                  </div>
                </div>
              );
            })}
          </div>
        </Card>

        {/* RIGHT : 답안 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <Card className="overflow-hidden">
            {/* 탭 */}
            <div className="flex items-center gap-0 overflow-x-auto border-b border-line-card bg-subtle px-2">
              {seq.slice(0, revealed).map((qq, i) => {
                const active = i === mtab;
                const done = state[i]?.checked;
                const ok = done && state[i].selected === qq.answer;
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => goTab(i)}
                    className={`flex items-center gap-1.5 border-b-2 px-4 py-3 text-[13px] transition-all ${
                      active
                        ? "border-ink font-bold text-ink"
                        : "border-transparent font-semibold text-soft"
                    }`}
                  >
                    {i === 0 ? "본 질문" : `꼬리 ${i}`}
                    <span className={`font-bold ${ok ? "text-success" : "text-danger"}`}>
                      {done ? (ok ? "✓" : "✕") : ""}
                    </span>
                  </button>
                );
              })}
            </div>

            <div className="flex items-center gap-2 px-[22px] pb-1.5 pt-[18px]">
              <Badge tone="accent" size="xs">
                {mtab === 0 ? "본 질문" : `꼬리질문 ${mtab}`}
              </Badge>
              <span className="text-xs font-semibold text-soft">
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
                let border: string = palette.line, bg: string = "#fff", numBg: string = palette.neutral, numColor: string = palette.muted, mark = "", markColor: string = "transparent";
                if (cell.checked) {
                  if (isAns) { border = palette.success; bg = palette.successBg; numBg = palette.success; numColor = "#fff"; mark = "정답"; markColor = palette.success; }
                  else if (sel) { border = palette.danger; bg = palette.dangerBg; numBg = palette.danger; numColor = "#fff"; mark = "오답"; markColor = palette.danger; }
                } else if (sel) { border = palette.accent; bg = palette.accentFaint; numBg = palette.accent; numColor = "#fff"; }
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
                <div className="mt-1.5 flex flex-col gap-3 border-t border-dashed border-line pt-4">
                  <div className={`text-[15px] font-bold ${ansOk ? "text-success" : "text-danger"}`}>
                    {ansOk ? "✓ 정답입니다" : "✕ 오답입니다 · 오답노트에 자동 저장됨"}
                  </div>
                  <div className="rounded-[12px] bg-subtle px-[18px] py-4">
                    <div className="mb-[7px] text-xs font-semibold text-muted">정답 해설</div>
                    <div className="text-[14px] leading-[1.65] text-body">{aq.explanation}</div>
                  </div>
                  {wrongPicked && aq.optExp?.[cell.selected as number] && (
                    <div className="rounded-[12px] border border-alert-line bg-alert-bg px-[18px] py-4">
                      <div className="mb-[7px] text-xs font-semibold text-alert">
                        내가 고른 답 — {(cell.selected as number) + 1}번 · 왜 틀렸나
                      </div>
                      <div className="text-[14px] leading-[1.65] text-alert-deep">
                        {aq.optExp[cell.selected as number]}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 푸터 */}
            <div className="flex justify-end gap-2 border-t border-line-card px-[22px] py-3.5">
              {/* 모든 질문을 다 풀면 종료하기 버튼을 숨긴다 (디자인: showEndQuiz) */}
              {!allAnswered && (
                <Button variant="muted" size="lg" onClick={() => onFinish(correct, seq.length)}>
                  종료하기
                </Button>
              )}
              {!cell.checked ? (
                <Button size="lg" onClick={check} disabled={cell.selected === null}>
                  정답 확인
                </Button>
              ) : !allAnswered ? (
                <Button size="lg" onClick={nextUnanswered}>
                  다음 질문
                </Button>
              ) : (
                /* 다 풀었을 때: 저장하고 문제은행으로 복귀 (디자인: 저장하기 → setup) */
                <Button size="lg" onClick={onQuit}>
                  저장하기
                </Button>
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
