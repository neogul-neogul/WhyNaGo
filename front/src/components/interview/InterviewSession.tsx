"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import type { InterviewAnswerSnapshotRequest, InterviewQuestionResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { CATEGORY_LABELS, DIFFICULTY_LABELS } from "@/lib/questions";
import { diffTone, lvBadge } from "@/lib/badges";
import {
  answerInterview,
  cancelInterview,
  completeInterview,
  ESSAY_AI_DAILY_QUOTA_EXCEEDED,
  ESSAY_AI_QUOTA_EXCEEDED,
  interviewItemLabel,
  QUOTA_COOLDOWN_SECONDS,
} from "@/lib/interviews";
import { useBeforeUnloadWarning, useFocusGuard } from "@/lib/focusGuard";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card, { CardHeader } from "@/components/ui/Card";
import GradingProgress from "@/components/solve/GradingProgress";
import QuestionTimer from "@/components/interview/QuestionTimer";
import FocusWarningDialog from "@/components/interview/FocusWarningDialog";

/** 채점이 끝난 문항 (발문·내 답변 + 채점 응답 스냅샷) */
interface GradedItem {
  question: string;
  answer: string;
  feedback: string;
  modelAnswer: string;
  isCorrect: boolean;
}

/** 재시도해도 회복되지 않거나 오늘 자리에 영향을 주는 AI 실패 */
const AI_ERROR_CODES = ["ESSAY_AI_UNAVAILABLE", ESSAY_AI_QUOTA_EXCEEDED, ESSAY_AI_DAILY_QUOTA_EXCEEDED];

const AUTO_SUBMIT_NOTICE_MS = 4000;

/**
 * 면접 진행 화면.
 *
 * 서술형 풀이(EssayQuiz)와 문답 흐름은 같지만 다음이 다르다.
 * - 세션 시작·대화 식별자·시작 시각을 서버가 소유해 여기서 다루지 않는다
 * - 문항마다 제한 시간이 있고, 만료되면 작성 중인 답변을 그대로 자동 제출한다
 * - 화면 이탈을 세어 완료 시 함께 보고한다
 * - "저장하기" 버튼이 없다 — 3문항 채점이 끝나면 자동으로 완료 처리한다
 * - "종료하기" 버튼이 없다 — 중도 이탈하면 오늘 자리를 잃으므로 이탈을 유도하지 않는다
 */
export default function InterviewSession({
  interviewId,
  question,
  totalQuestionCount,
  timeLimitSeconds,
  onCompleted,
  onAborted,
}: {
  interviewId: number;
  question: InterviewQuestionResponse;
  totalQuestionCount: number;
  timeLimitSeconds: number;
  /** 완료 저장까지 성공했을 때 (결과 화면으로 이동) */
  onCompleted: () => void;
  /** 한 문항도 채점받지 못해 면접을 취소했을 때 (안내 페이지로 복귀) */
  onAborted: (message: string) => void;
}) {
  const [items, setItems] = useState<GradedItem[]>([]);
  // 답변 중인 문항 발문. null이면 3문항을 모두 마쳤다는 뜻
  const [current, setCurrent] = useState<string | null>(question.content);
  const [draft, setDraft] = useState("");
  const [grading, setGrading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [quota, setQuota] = useState<{ daily: boolean; message: string } | null>(null);
  const [cooldown, setCooldown] = useState(0);
  const [focusLossCount, setFocusLossCount] = useState(0);
  const [showFocusWarning, setShowFocusWarning] = useState(false);
  const [autoSubmitted, setAutoSubmitted] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const done = current === null;
  const idx = items.length;
  const correctCount = items.filter((item) => item.isCorrect).length;
  const dailyQuotaReached = quota?.daily === true;
  // 타이머 만료 콜백은 등록 시점의 draft를 붙잡고 있으므로 최신값을 ref로 따라간다
  const draftRef = useRef(draft);
  useEffect(() => {
    draftRef.current = draft;
  }, [draft]);
  const gradingRef = useRef(false);

  useFocusGuard({
    active: !done && !grading && !dailyQuotaReached,
    onLeave: () => {
      setFocusLossCount((count) => count + 1);
      setShowFocusWarning(true);
    },
  });
  useBeforeUnloadWarning(!done);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
    return () => clearTimeout(timer);
  }, [cooldown]);

  useEffect(() => {
    if (!autoSubmitted) return;
    const timer = setTimeout(() => setAutoSubmitted(false), AUTO_SUBMIT_NOTICE_MS);
    return () => clearTimeout(timer);
  }, [autoSubmitted]);

  /** 한 문항도 채점받지 못한 상태의 AI 실패 — 오늘 자리를 돌려주고 안내 페이지로 */
  const cancelAndExit = async (message: string) => {
    try {
      await cancelInterview(interviewId);
    } catch {
      // 취소 실패는 삼킨다. 서버 상태가 진실이므로 다음 진입 시 상태 조회가 정정한다
    }
    onAborted(message);
  };

  const submit = async (auto: boolean) => {
    if (gradingRef.current || !current || cooldown > 0 || dailyQuotaReached) return;
    // 제한 시간 만료로 인한 자동 제출만 빈 답변을 허용한다 (D3)
    const answer = draftRef.current.trim();
    if (!auto && !answer) return;

    gradingRef.current = true;
    setGrading(true);
    setError(null);
    setQuota(null);
    if (auto) setAutoSubmitted(true);

    try {
      const result = await answerInterview(interviewId, { question: current, answer });
      setItems((prev) => [
        ...prev,
        {
          question: current,
          answer,
          feedback: result.grading.feedback,
          modelAnswer: result.grading.modelAnswer,
          isCorrect: result.grading.isCorrect,
        },
      ]);
      setCurrent(result.nextFollowup?.question ?? null);
      setDraft("");
    } catch (e) {
      // 실패해도 draft를 지우지 않는다 — 같은 답변으로 바로 재시도할 수 있게
      const code = e instanceof ApiError ? e.code : null;
      const message =
        e instanceof ApiError ? e.message : "채점에 실패했습니다. 다시 시도해주세요.";

      // 채점 성공 0건이면 오늘 자리를 소진시키지 않고 취소한다 (D13)
      if (items.length === 0 && code !== null && AI_ERROR_CODES.includes(code)) {
        await cancelAndExit(
          "일시적인 문제로 면접을 시작하지 못했어요. 오늘 다시 시도할 수 있습니다.",
        );
        return;
      }

      if (code === ESSAY_AI_QUOTA_EXCEEDED || code === ESSAY_AI_DAILY_QUOTA_EXCEEDED) {
        const daily = code === ESSAY_AI_DAILY_QUOTA_EXCEEDED;
        setQuota({ daily, message });
        setCooldown(daily ? 0 : QUOTA_COOLDOWN_SECONDS);
      } else {
        setError(message);
      }
    } finally {
      gradingRef.current = false;
      setGrading(false);
    }
  };

  const complete = async () => {
    if (saving) return;
    setSaving(true);
    setSaveError(null);
    const toSnapshot = (item: GradedItem): InterviewAnswerSnapshotRequest => ({
      questionText: item.question,
      userAnswer: item.answer,
      feedback: item.feedback,
      modelAnswer: item.modelAnswer,
      isCorrect: item.isCorrect,
    });
    try {
      await completeInterview(interviewId, {
        rootQuestion: toSnapshot(items[0]),
        followupQuestions: items.slice(1).map(toSnapshot),
        focusLossCount,
      });
      onCompleted();
    } catch (e) {
      // 진행분은 state에 그대로 남아 있어 재시도로 그대로 올릴 수 있다
      setSaveError(
        e instanceof ApiError ? e.message : "결과를 저장하지 못했습니다. 다시 시도해주세요.",
      );
    } finally {
      setSaving(false);
    }
  };

  // 3문항 채점이 끝나면 저장은 선택이 아니다 — 자동으로 완료 처리한다
  const completeStartedRef = useRef(false);
  useEffect(() => {
    if (!done || items.length !== totalQuestionCount || completeStartedRef.current) return;
    completeStartedRef.current = true;
    void complete();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [done, items.length, totalQuestionCount]);

  // 화면에 노출되는 문항 시퀀스 = 채점 완료된 문항들 + 답변 중 문항
  const seq = [...items.map((item) => item.question), ...(current ? [current] : [])];

  return (
    <div className="flex flex-col gap-4">
      {/* 진행 상태 바 — 돌아가기 링크를 두지 않는다 (이탈 시 오늘 자리를 잃는다) */}
      <div className="flex items-center gap-2.5">
        <Badge tone="ink">
          문항 {Math.min(idx + 1, totalQuestionCount)} / {totalQuestionCount}
        </Badge>
        {!done && (
          <QuestionTimer
            questionIndex={idx}
            seconds={timeLimitSeconds}
            paused={grading || cooldown > 0 || dailyQuotaReached}
            onExpire={() => void submit(true)}
          />
        )}
        {focusLossCount > 0 && (
          <Badge tone="alert">화면 이탈 {focusLossCount}회</Badge>
        )}
        <span className="ml-auto text-[12.5px] font-semibold text-soft">
          중간에 나가면 오늘 면접이 사라져요
        </span>
      </div>

      <div className="flex items-start gap-[18px]">
        {/* LEFT : 질문 목록 */}
        <Card className="min-w-0 flex-1 overflow-hidden">
          <CardHeader className="gap-2.5">
            <Badge tone="ai">1일 1면접</Badge>
            <Badge tone={diffTone(DIFFICULTY_LABELS[question.difficulty])}>
              난이도 {lvBadge(DIFFICULTY_LABELS[question.difficulty])}
            </Badge>
            <Badge tone="neutral" className="ml-auto">
              {CATEGORY_LABELS[question.category]}
            </Badge>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-6">
            {seq.map((text, i) => {
              const isActive = !done && i === idx;
              return (
                <div
                  key={i}
                  className={`flex flex-col gap-2 rounded-[12px] border px-4 py-3.5 ${
                    isActive ? "border-accent-line bg-accent-faint" : "border-line-card bg-white"
                  }`}
                >
                  <span
                    className={`inline-flex w-fit items-center rounded-[5px] px-[9px] py-0.5 text-[11px] font-bold ${
                      i < items.length ? "bg-success-bg text-success" : "bg-accent-bg text-accent"
                    }`}
                  >
                    {interviewItemLabel(i)}
                  </span>
                  <div className="text-[15.5px] font-semibold leading-[1.55] text-ink">{text}</div>
                </div>
              );
            })}
          </div>
        </Card>

        {/* RIGHT : 답변 */}
        <div className="flex min-w-0 flex-[1.15] flex-col gap-3.5">
          <Card className="overflow-hidden">
            <CardHeader className="gap-2">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-[5px] bg-ink text-[10px] font-bold text-white">
                AI
              </span>
              <span className="text-[13px] font-bold text-secondary">
                AI 면접관에게 답하듯 작성하세요
              </span>
            </CardHeader>
            <div className="flex flex-col gap-3.5 px-[22px] py-5">
              {/* 답변 완료 기록 */}
              {items.map((item, i) => (
                <div key={i} className="overflow-hidden rounded-[12px] border border-line-card">
                  <div className="flex items-center gap-1.5 border-b border-line-soft bg-subtle px-4 py-[9px]">
                    <span className="inline-flex items-center rounded-[5px] bg-neutral px-2 py-0.5 text-[10.5px] font-bold text-secondary">
                      {interviewItemLabel(i)} · 내 답변
                    </span>
                    <span
                      className={`ml-auto text-[12px] font-bold ${
                        item.isCorrect ? "text-success" : "text-danger"
                      }`}
                    >
                      {item.isCorrect ? "✓ 통과" : "✕ 미통과"}
                    </span>
                  </div>
                  <div className="whitespace-pre-wrap px-4 py-[13px] text-[14px] leading-[1.65] text-body">
                    {item.answer || (
                      <span className="text-soft">답변을 작성하지 못한 채 시간이 만료되었어요.</span>
                    )}
                  </div>
                  {/* 진행 중에는 피드백을 감춘다 — 꼬리질문이 직전 답변에서 생성되므로
                      피드백을 읽고 답하면 이해도가 아니라 힌트 반영도를 재게 된다.
                      다음 문항 타이머가 이미 흐르고 있어 읽는 시간이 손해가 되는 문제도 있다. */}
                  <div className="flex items-center gap-1.5 border-t border-line-soft bg-white px-4 py-2.5 text-[12.5px] font-semibold text-soft">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                      <path d="M12 2a10 10 0 100 20 10 10 0 000-20z" />
                      <path d="M12 16v-4M12 8h.01" />
                    </svg>
                    AI 피드백과 모범답안은 면접이 끝나면 한 번에 볼 수 있어요
                  </div>
                </div>
              ))}

              {/* 제한 시간 만료로 자동 제출됨 */}
              {autoSubmitted && (
                <div className="rounded-[12px] bg-warning-bg px-4 py-3 text-[13px] font-semibold text-warning" role="status">
                  제한 시간이 끝나 작성 중이던 답변을 자동으로 제출했어요.
                </div>
              )}

              {grading && <GradingProgress />}

              {/* AI 채점 쿼터 초과 — 분당 한도는 쿨다운 후 재시도, 일일 한도는 진행 불가 */}
              {quota && (
                <div className="flex flex-col items-start gap-2 rounded-[12px] border border-alert-line bg-alert-bg px-4 py-3.5">
                  <div className="text-[13.5px] font-semibold text-alert-deep">{quota.message}</div>
                  {quota.daily ? (
                    <>
                      <div className="text-[12.5px] text-alert-deep">
                        오늘은 면접을 이어갈 수 없어요. 지금까지 답한 내용은 저장되지 않습니다.
                      </div>
                      <Link
                        href="/"
                        className="rounded-[10px] border border-line-strong bg-white px-[22px] py-[11px] text-[13.5px] font-semibold text-ink transition-colors hover:border-ink"
                      >
                        홈으로
                      </Link>
                    </>
                  ) : (
                    <div className="text-[12.5px] font-semibold text-alert-deep">
                      {cooldown > 0
                        ? `${cooldown}초 후 같은 답변으로 다시 제출할 수 있어요. 그동안 타이머는 멈춥니다.`
                        : "이제 다시 제출할 수 있어요."}
                    </div>
                  )}
                </div>
              )}

              {/* 현재 질문 답변 입력 */}
              {!done && !dailyQuotaReached && (
                <div className="flex flex-col gap-[11px]">
                  <div className="flex items-center gap-1.5">
                    <Badge tone="accent" size="xs">
                      {interviewItemLabel(idx)}
                    </Badge>
                    <span className="text-xs font-semibold text-soft">에 답변해 주세요</span>
                  </div>
                  <textarea
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                    disabled={grading}
                    placeholder="면접관에게 답하듯 설명해 보세요. 근거와 예시를 함께 들면 좋습니다."
                    className="block min-h-[170px] w-full resize-y rounded-[12px] border border-line-input bg-white px-4 py-3.5 text-[14.5px] leading-[1.7] text-ink outline-none disabled:bg-subtle disabled:text-soft"
                  />
                  <span className="font-mono text-xs text-placeholder">{draft.length}자</span>
                </div>
              )}

              {/* 3문항 완료 — 저장은 자동으로 진행된다 */}
              {done && (
                <div className="flex flex-col gap-2 border-t border-dashed border-line pt-4">
                  <div className="flex items-center gap-1.5 text-[15px] font-bold text-success">
                    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 6L9 17l-5-5" />
                    </svg>
                    면접 완료 · 꼬리질문까지 모두 답했어요
                  </div>
                  <div className="text-[13px] text-soft">
                    통과 {correctCount} / {items.length} · 화면 이탈 {focusLossCount}회
                  </div>
                  {saveError ? (
                    <div className="flex flex-col items-start gap-2.5 rounded-[12px] border border-alert-line bg-alert-bg px-4 py-3.5">
                      <div className="text-[13.5px] font-semibold text-alert-deep">{saveError}</div>
                      <Button variant="secondary" onClick={complete} disabled={saving}>
                        {saving ? "저장 중…" : "다시 저장"}
                      </Button>
                    </div>
                  ) : (
                    <div className="text-[13px] font-semibold text-secondary">
                      결과를 저장하고 있어요…
                    </div>
                  )}
                </div>
              )}
            </div>

            {!done && (
              <div className="flex items-center justify-end gap-2 border-t border-line-card px-[22px] py-3.5">
                {error && (
                  <span className="mr-auto text-[13px] font-semibold text-danger">{error}</span>
                )}
                <Button
                  size="lg"
                  onClick={() => void submit(false)}
                  disabled={!draft.trim() || grading || cooldown > 0 || dailyQuotaReached}
                >
                  {grading ? (
                    <span className="flex items-center gap-[7px]">
                      <span
                        className="h-3 w-3 animate-spin rounded-full border-2 border-white/40 border-t-white"
                        aria-hidden="true"
                      />
                      채점 중…
                    </span>
                  ) : cooldown > 0 ? (
                    `${cooldown}초 후 재시도`
                  ) : (
                    "답변 제출"
                  )}
                </Button>
              </div>
            )}
          </Card>
        </div>
      </div>

      {showFocusWarning && (
        <FocusWarningDialog
          count={focusLossCount}
          onClose={() => setShowFocusWarning(false)}
        />
      )}
    </div>
  );
}
