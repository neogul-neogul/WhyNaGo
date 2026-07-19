"use client";

import { useState } from "react";
import type { WrongNote } from "@/types";
import { diffTone } from "@/lib/badges";
import { palette } from "@/lib/tokens";
import Badge from "@/components/ui/Badge";
import Card, { CardHeader } from "@/components/ui/Card";

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
            <Badge tone={diffTone(note.diff)}>난이도 {note.diff}</Badge>
            <Badge tone="neutral" className="ml-auto">{note.cat}</Badge>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {seq.map((qq, i) => {
              const active = i === wtab;
              const done = qq.myAnswer === qq.answer;
              return (
                <div
                  key={i}
                  onClick={() => setTab(i)}
                  className={`flex cursor-pointer flex-col gap-2 rounded-[12px] border px-4 py-3.5 ${
                    active ? "border-accent-line bg-accent-faint" : "border-line-card bg-white"
                  }`}
                >
                  <span
                    className={`inline-flex w-fit items-center rounded-[5px] px-[9px] py-0.5 text-[11px] font-bold ${
                      done ? "bg-success-bg text-success" : "bg-danger-bg text-danger"
                    }`}
                  >
                    {i === 0 ? "본 질문" : `꼬리질문 ${i}`}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">{qq.text}</div>
                </div>
              );
            })}
            <div className="text-xs text-placeholder">
              {note.source} · {note.solvedAt}
            </div>
          </div>
        </Card>

        {/* RIGHT : 답안 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <Card className="overflow-hidden">
            <div className="flex items-center gap-0 overflow-x-auto border-b border-line-card bg-subtle px-2.5">
              {seq.map((qq, i) => {
                const active = i === wtab;
                const done = qq.myAnswer === qq.answer;
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => setTab(i)}
                    className={`flex items-center gap-1.5 border-b-2 px-4 py-3 text-[13px] transition-all ${
                      active
                        ? "border-ink font-bold text-ink"
                        : "border-transparent font-semibold text-soft"
                    }`}
                  >
                    {i === 0 ? "본 질문" : `꼬리 ${i}`}
                    <span className={`font-bold ${done ? "text-success" : "text-danger"}`}>
                      {done ? "✓" : "✕"}
                    </span>
                  </button>
                );
              })}
              <button
                type="button"
                onClick={onToggleBookmark}
                title="북마크"
                className={`ml-auto flex p-0.5 ${bookmarked ? "text-accent" : "text-icon"}`}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill={bookmarked ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
                </svg>
              </button>
            </div>

            <div className="flex items-center gap-2 px-[22px] pb-1.5 pt-[18px]">
              <Badge tone="accent" size="xs">
                {wtab === 0 ? "본 질문" : `꼬리질문 ${wtab}`}
              </Badge>
              <span className="text-xs font-semibold text-soft">
                {wtab === 0
                  ? "개념을 묻는 본 질문입니다"
                  : `앞선 답변에서 파생된 꼬리질문 ${wtab}/${seq.length - 1}`}
              </span>
            </div>

            <div className="flex flex-col gap-2.5 px-[22px] pb-5 pt-3.5">
              {aq.options.map((opt, oi) => {
                const sel = oi === aq.myAnswer;
                const isAns = oi === aq.answer;
                let border: string = palette.line, bg: string = "#fff", numBg: string = palette.neutral, numColor: string = palette.muted, mark = "", markColor: string = "transparent";
                if (isAns) { border = palette.success; bg = palette.successBg; numBg = palette.success; numColor = "#fff"; mark = "정답"; markColor = palette.success; }
                else if (sel) { border = palette.danger; bg = palette.dangerBg; numBg = palette.danger; numColor = "#fff"; mark = "오답"; markColor = palette.danger; }
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
              <div className="mt-1.5 flex flex-col gap-3 border-t border-dashed border-line pt-4">
                <div className={`text-[15px] font-bold ${aqWrong ? "text-danger" : "text-success"}`}>
                  {aqWrong ? "✕ 오답입니다 · 오답노트에 자동 저장됨" : "✓ 정답입니다"}
                </div>
                <div className="rounded-[12px] bg-subtle px-[18px] py-4">
                  <div className="mb-[7px] text-xs font-semibold text-muted">정답 해설</div>
                  <div className="text-[14px] leading-[1.65] text-body">{aq.explanation}</div>
                </div>
                {aqWrong && aq.wrongExp && (
                  <div className="rounded-[12px] border border-alert-line bg-alert-bg px-[18px] py-4">
                    <div className="mb-[7px] text-xs font-semibold text-alert">
                      내가 고른 답 — {aq.myAnswer + 1}번 · 왜 틀렸나
                    </div>
                    <div className="text-[14px] leading-[1.65] text-alert-deep">{aq.wrongExp}</div>
                  </div>
                )}
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
