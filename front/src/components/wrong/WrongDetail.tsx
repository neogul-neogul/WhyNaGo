"use client";

import { useState } from "react";
import type { WrongNoteDetailResponse } from "@/types";
import { CATEGORY_LABELS, DIFFICULTY_LABELS } from "@/lib/questions";
import { formatSolvedDate } from "@/lib/wrongNotes";
import { diffTone } from "@/lib/badges";
import { palette } from "@/lib/tokens";
import Badge from "@/components/ui/Badge";
import Card, { CardHeader } from "@/components/ui/Card";

// 오답 상세 (문제 풀이 꼬리질문 완료 화면과 동일 형태, 모두 채점된 상태)
// 유형에 따라 note.multipleChoiceItems 또는 note.essayItems 중 하나만 채워진다
export default function WrongDetail({
  note,
  onToggleBookmark,
  onBack,
}: {
  note: WrongNoteDetailResponse;
  onToggleBookmark: () => void;
  onBack: () => void;
}) {
  const isEssay = note.type === "ESSAY";
  const items = isEssay ? note.essayItems ?? [] : note.multipleChoiceItems ?? [];
  const diffLabel = DIFFICULTY_LABELS[note.difficulty];

  const [tab, setTab] = useState(0);
  const wtab = Math.min(tab, items.length - 1);

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
            <Badge tone={isEssay ? "ai" : "accent"}>{isEssay ? "서술형" : "객관식"}</Badge>
            <Badge tone={diffTone(diffLabel)}>난이도 {diffLabel}</Badge>
            <Badge tone="neutral" className="ml-auto">{CATEGORY_LABELS[note.category]}</Badge>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {items.map((qq, i) => {
              const active = i === wtab;
              const text = isEssay ? (qq as { questionText: string }).questionText : (qq as { content: string }).content;
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
                      qq.isCorrect ? "bg-success-bg text-success" : "bg-danger-bg text-danger"
                    }`}
                  >
                    {i === 0 ? "본 질문" : `꼬리질문 ${i}`}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">{text}</div>
                </div>
              );
            })}
            <div className="text-xs text-placeholder">{formatSolvedDate(note.solvedAt)}</div>
          </div>
        </Card>

        {/* RIGHT : 답안 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <Card className="overflow-hidden">
            <div className="flex items-center gap-0 overflow-x-auto border-b border-line-card bg-subtle px-2.5">
              {items.map((qq, i) => {
                const active = i === wtab;
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
                    <span className={`font-bold ${qq.isCorrect ? "text-success" : "text-danger"}`}>
                      {qq.isCorrect ? "✓" : "✕"}
                    </span>
                  </button>
                );
              })}
              <button
                type="button"
                onClick={onToggleBookmark}
                title="북마크"
                className={`ml-auto flex p-0.5 ${note.isBookmarked ? "text-accent" : "text-icon"}`}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill={note.isBookmarked ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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
                  : `앞선 답변에서 파생된 꼬리질문 ${wtab}/${items.length - 1}`}
              </span>
            </div>

            {isEssay ? (
              <EssayAnswerPanel item={note.essayItems![wtab]} />
            ) : (
              <MultipleChoiceAnswerPanel item={note.multipleChoiceItems![wtab]} />
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}

function MultipleChoiceAnswerPanel({
  item,
}: {
  item: NonNullable<WrongNoteDetailResponse["multipleChoiceItems"]>[number];
}) {
  return (
    <div className="flex flex-col gap-2.5 px-[22px] pb-5 pt-3.5">
      {item.choices.map((choice) => {
        const sel = choice.id === item.userChoiceId;
        let border: string = palette.line, bg: string = "#fff", numBg: string = palette.neutral, numColor: string = palette.muted, mark = "", markColor: string = "transparent";
        if (choice.isCorrect) { border = palette.success; bg = palette.successBg; numBg = palette.success; numColor = "#fff"; mark = "정답"; markColor = palette.success; }
        else if (sel) { border = palette.danger; bg = palette.dangerBg; numBg = palette.danger; numColor = "#fff"; mark = "오답"; markColor = palette.danger; }
        return (
          <div
            key={choice.id}
            className="flex w-full items-center gap-[13px] rounded-[12px] px-[17px] py-[15px]"
            style={{ border: `1.5px solid ${border}`, background: bg }}
          >
            <span
              className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full text-[12.5px] font-bold"
              style={{ background: numBg, color: numColor }}
            >
              {choice.sequence}
            </span>
            <span className="flex-1 text-[14.5px] leading-[1.5]">{choice.content}</span>
            <span className="text-[13px] font-bold" style={{ color: markColor }}>{mark}</span>
          </div>
        );
      })}
      <div className="mt-1.5 flex flex-col gap-3 border-t border-dashed border-line pt-4">
        <div className={`text-[15px] font-bold ${item.isCorrect ? "text-success" : "text-danger"}`}>
          {item.isCorrect ? "✓ 정답입니다" : "✕ 오답입니다 · 오답노트에 자동 저장됨"}
        </div>
        <div className="rounded-[12px] bg-subtle px-[18px] py-4">
          <div className="mb-[7px] text-xs font-semibold text-muted">정답 해설</div>
          <div className="text-[14px] leading-[1.65] text-body">{item.explanation}</div>
        </div>
        {!item.isCorrect && item.choiceExplanation && (
          <div className="rounded-[12px] border border-alert-line bg-alert-bg px-[18px] py-4">
            <div className="mb-[7px] text-xs font-semibold text-alert">
              내가 고른 답 — {item.choices.find((c) => c.id === item.userChoiceId)?.sequence}번 · 왜 틀렸나
            </div>
            <div className="text-[14px] leading-[1.65] text-alert-deep">{item.choiceExplanation}</div>
          </div>
        )}
      </div>
    </div>
  );
}

function EssayAnswerPanel({
  item,
}: {
  item: NonNullable<WrongNoteDetailResponse["essayItems"]>[number];
}) {
  return (
    <div className="flex flex-col gap-3 px-[22px] pb-5 pt-3.5">
      <div className="rounded-[12px] border border-line-card bg-white px-[18px] py-4">
        <div className="mb-[7px] text-xs font-semibold text-muted">내 답변</div>
        <div className="whitespace-pre-wrap text-[14px] leading-[1.65] text-body">{item.userAnswer}</div>
      </div>
      <div className={`text-[15px] font-bold ${item.isCorrect ? "text-success" : "text-danger"}`}>
        {item.isCorrect ? "✓ 정답입니다" : "✕ 오답입니다 · 오답노트에 자동 저장됨"}
      </div>
      <div className="rounded-[12px] border border-alert-line bg-alert-bg px-[18px] py-4">
        <div className="mb-[7px] text-xs font-semibold text-alert">AI 피드백</div>
        <div className="text-[14px] leading-[1.65] text-alert-deep">{item.feedback}</div>
      </div>
      <div className="rounded-[12px] bg-success-bg px-[18px] py-4">
        <div className="mb-[7px] text-xs font-semibold text-success">모범답안 · 해설</div>
        <div className="text-[14px] leading-[1.65] text-body">{item.modelAnswer}</div>
      </div>
    </div>
  );
}
