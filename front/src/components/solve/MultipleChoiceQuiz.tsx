"use client";

import { useState } from "react";
import type { ChoiceGradingResponse, QuestionResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { CATEGORY_LABELS, DIFFICULTY_LABELS, gradeQuestion, saveSolvedSession } from "@/lib/questions";
import { diffTone, lvBadge } from "@/lib/badges";
import { palette } from "@/lib/tokens";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card, { CardHeader } from "@/components/ui/Card";

/** 채점이 끝난 문항 (본질문/꼬리질문 각 1개) */
interface SolvedItem {
  question: QuestionResponse;
  selectedChoiceId: number;
  grading: ChoiceGradingResponse;
}

// 객관식 풀이 (프로그래머스식 좌우 분할 + 꼬리질문 탭)
// 꼬리질문은 고른 보기의 채점 응답(nextQuestion)으로 이어진다 — 보기별 분기
export default function MultipleChoiceQuiz({
  question,
  onQuit,
  onFinish,
}: {
  question: QuestionResponse;
  onQuit: () => void;
  onFinish: (correct: number, total: number) => void;
}) {
  const [solvedItems, setSolvedItems] = useState<SolvedItem[]>([]);
  // 풀이 중인 문항. null이면 체인 종료(모두 채점됨)
  const [current, setCurrent] = useState<QuestionResponse | null>(question);
  const [selectedChoiceId, setSelectedChoiceId] = useState<number | null>(null);
  const [tab, setTab] = useState(0);
  const [grading, setGrading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 화면에 노출되는 문항 시퀀스 = 채점 완료된 문항들 + 풀이 중 문항
  const seq: QuestionResponse[] = [
    ...solvedItems.map((item) => item.question),
    ...(current ? [current] : []),
  ];
  const mtab = Math.min(tab, seq.length - 1);
  const viewedQuestion = seq[mtab];
  const viewedItem = mtab < solvedItems.length ? solvedItems[mtab] : null;
  const allAnswered = current === null;
  const correctCount = solvedItems.filter((item) => item.grading.correct).length;

  const select = (choiceId: number) => {
    // 채점 완료 문항은 선택 변경 불가 (1문항 1회 응답)
    if (viewedItem || grading) return;
    setSelectedChoiceId(choiceId);
  };

  const check = async () => {
    if (!current || selectedChoiceId === null || grading) return;
    setGrading(true);
    setError(null);
    try {
      const result = await gradeQuestion(current.id, selectedChoiceId);
      setSolvedItems((items) => [
        ...items,
        { question: current, selectedChoiceId, grading: result },
      ]);
      setCurrent(result.nextQuestion);
      setSelectedChoiceId(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "채점에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setGrading(false);
    }
  };

  const save = async () => {
    if (saving || solvedItems.length === 0) return;
    setSaving(true);
    setError(null);
    const toRequest = (item: SolvedItem) => ({
      questionId: item.question.id,
      choiceId: item.selectedChoiceId,
      relationQuestionId: item.grading.nextQuestion?.id ?? null,
    });
    try {
      await saveSolvedSession({
        rootQuestion: toRequest(solvedItems[0]),
        followupQuestions: solvedItems.slice(1).map(toRequest),
      });
      onFinish(correctCount, solvedItems.length);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "저장에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setSaving(false);
    }
  };

  const goTab = (i: number) => {
    if (i < seq.length) setTab(i);
  };
  const goNext = () => {
    setTab(solvedItems.length);
  };

  const answeredOk = viewedItem?.grading.correct ?? false;
  const wrongPicked = viewedItem !== null && !viewedItem.grading.correct;
  const selectedChoice = viewedItem
    ? viewedItem.question.choices.find((c) => c.id === viewedItem.selectedChoiceId)
    : null;

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
            <Badge tone={diffTone(DIFFICULTY_LABELS[question.difficulty])}>
              난이도 {lvBadge(DIFFICULTY_LABELS[question.difficulty])}
            </Badge>
            <Badge tone="neutral" className="ml-auto">
              {CATEGORY_LABELS[question.category]}
            </Badge>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {seq.map((qq, i) => {
              const active = i === mtab;
              const done = i < solvedItems.length;
              return (
                <div
                  key={qq.id}
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
                    {qq.content}
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
              {seq.map((qq, i) => {
                const active = i === mtab;
                const item = solvedItems[i];
                return (
                  <button
                    key={qq.id}
                    type="button"
                    onClick={() => goTab(i)}
                    className={`flex items-center gap-1.5 border-b-2 px-4 py-3 text-[13px] transition-all ${
                      active
                        ? "border-ink font-bold text-ink"
                        : "border-transparent font-semibold text-soft"
                    }`}
                  >
                    {i === 0 ? "본 질문" : `꼬리 ${i}`}
                    <span className={`font-bold ${item?.grading.correct ? "text-success" : "text-danger"}`}>
                      {item ? (item.grading.correct ? "✓" : "✕") : ""}
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
                  : "앞선 답변에서 파생된 꼬리질문입니다"}
              </span>
            </div>

            {/* 선택지 */}
            <div className="flex flex-col gap-2.5 px-[22px] pb-5 pt-3.5">
              {viewedQuestion.choices.map((choice) => {
                const sel = viewedItem
                  ? choice.id === viewedItem.selectedChoiceId
                  : choice.id === selectedChoiceId;
                const isAns = viewedItem !== null && choice.id === viewedItem.grading.correctChoiceId;
                let border: string = palette.line, bg: string = "#fff", numBg: string = palette.neutral, numColor: string = palette.muted, mark = "", markColor: string = "transparent";
                if (viewedItem) {
                  if (isAns) { border = palette.success; bg = palette.successBg; numBg = palette.success; numColor = "#fff"; mark = "정답"; markColor = palette.success; }
                  else if (sel) { border = palette.danger; bg = palette.dangerBg; numBg = palette.danger; numColor = "#fff"; mark = "오답"; markColor = palette.danger; }
                } else if (sel) { border = palette.accent; bg = palette.accentFaint; numBg = palette.accent; numColor = "#fff"; }
                return (
                  <button
                    key={choice.id}
                    type="button"
                    onClick={() => select(choice.id)}
                    className="flex w-full items-center gap-[13px] rounded-[12px] px-[17px] py-[15px] text-left transition-all"
                    style={{ border: `1.5px solid ${border}`, background: bg, cursor: viewedItem ? "default" : "pointer" }}
                  >
                    <span
                      className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full text-[12.5px] font-bold"
                      style={{ background: numBg, color: numColor }}
                    >
                      {choice.sequence}
                    </span>
                    <span className="flex-1 text-[14.5px] leading-[1.5]">{choice.content}</span>
                    <span className="text-[13px] font-bold" style={{ color: markColor }}>{mark}</span>
                  </button>
                );
              })}

              {viewedItem && (
                <div className="mt-1.5 flex flex-col gap-3 border-t border-dashed border-line pt-4">
                  <div className={`text-[15px] font-bold ${answeredOk ? "text-success" : "text-danger"}`}>
                    {answeredOk ? "✓ 정답입니다" : "✕ 오답입니다 · 오답노트에 자동 저장됨"}
                  </div>
                  <div className="rounded-[12px] bg-subtle px-[18px] py-4">
                    <div className="mb-[7px] text-xs font-semibold text-muted">정답 해설</div>
                    <div className="text-[14px] leading-[1.65] text-body">
                      {viewedItem.grading.explanation}
                    </div>
                  </div>
                  {wrongPicked && viewedItem.grading.choiceExplanation && (
                    <div className="rounded-[12px] border border-alert-line bg-alert-bg px-[18px] py-4">
                      <div className="mb-[7px] text-xs font-semibold text-alert">
                        내가 고른 답 — {selectedChoice?.sequence}번 · 왜 틀렸나
                      </div>
                      <div className="text-[14px] leading-[1.65] text-alert-deep">
                        {viewedItem.grading.choiceExplanation}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 푸터 */}
            <div className="flex items-center justify-end gap-2 border-t border-line-card px-[22px] py-3.5">
              {error && (
                <span className="mr-auto text-[13px] font-semibold text-danger">{error}</span>
              )}
              {/* 중도 이탈: 저장하지 않고 문제은행으로 복귀 */}
              {!allAnswered && (
                <Button variant="muted" size="lg" onClick={onQuit}>
                  종료하기
                </Button>
              )}
              {!viewedItem && !allAnswered ? (
                <Button size="lg" onClick={check} disabled={selectedChoiceId === null || grading}>
                  {grading ? "채점 중…" : "정답 확인"}
                </Button>
              ) : !allAnswered ? (
                <Button size="lg" onClick={goNext}>
                  다음 질문
                </Button>
              ) : (
                /* 마지막 문항까지 답했을 때만 세션 저장 */
                <Button size="lg" onClick={save} disabled={saving}>
                  {saving ? "저장 중…" : "저장하기"}
                </Button>
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
