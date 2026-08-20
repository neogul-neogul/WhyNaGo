"use client";

import { useEffect, useRef, useState } from "react";
import type { EssaySolvedQuestionRequest, MasteryLevel, QuestionResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import {
  CATEGORY_LABELS,
  DIFFICULTY_LABELS,
  evaluateEssayAnswer,
  nowAsLocalDateTime,
  saveEssaySolvedSession,
  startEssaySession,
} from "@/lib/questions";
import { diffTone, lvBadge } from "@/lib/badges";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card, { CardHeader } from "@/components/ui/Card";
import GradingProgress from "@/components/solve/GradingProgress";
import LoginRequiredGate from "@/components/layout/LoginRequiredGate";
import MasteryVerdict from "@/components/mastery/MasteryVerdict";
import SaveToProblemSetButton from "@/components/problemSets/SaveToProblemSetButton";

/** 채점이 끝난 문항 (발문·내 답변 + 채점 응답 스냅샷) */
interface GradedItem {
  question: string;
  answer: string;
  feedback: string;
  /** 화면에는 표시하지 않지만 저장 API의 필수값이라 응답값을 보관해 그대로 전달한다 */
  modelAnswer: string;
  isCorrect: boolean;
  /** 이 답변이 드러낸 이해 수준. AI가 판정하지 못하면 null */
  mastery: MasteryLevel | null;
  masteryReason: string | null;
}

const label = (i: number) => (i === 0 ? "본 질문" : `꼬리질문 ${i}`);

const QUOTA_COOLDOWN_SECONDS = 60;
const QUOTA_CODE = "ESSAY_AI_QUOTA_EXCEEDED";
const DAILY_QUOTA_CODE = "ESSAY_AI_DAILY_QUOTA_EXCEEDED";

// 서술형 풀이 (AI 면접식 꼬리질문)
// 꼬리질문은 미리 알 수 없고 채점 응답(nextFollowup)으로만 도착한다 — 문항 시퀀스가 응답으로 성장한다
export default function EssayQuiz({
  question,
  onQuit,
  onFinish,
  saved,
  onOpenSaveModal,
}: {
  question: QuestionResponse;
  onQuit: () => void;
  onFinish: (correct: number, total: number) => void;
  saved: boolean;
  onOpenSaveModal: () => void;
}) {
  const loggedIn = useAuth();
  const [showLoginGate, setShowLoginGate] = useState(false);
  const [conversationId, setConversationId] = useState<string | null>(null);
  // 본질문을 처음 받은 시각(세션 시작 시각). 부모가 문항마다 key로 컴포넌트를 새로 마운트하므로 1회만 계산된다
  const [startedAt] = useState(() => nowAsLocalDateTime());
  const [startError, setStartError] = useState<string | null>(null);
  const [items, setItems] = useState<GradedItem[]>([]);
  // 답변 중인 문항 발문. null이면 면접 종료(서버가 nextFollowup을 주지 않음)
  const [current, setCurrent] = useState<string | null>(question.content);
  const [draft, setDraft] = useState("");
  const [open, setOpen] = useState<number[]>([]);
  const [grading, setGrading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // 쿼터 초과는 재시도해도 즉시 회복되지 않아 일반 실패와 분리해 다룬다
  const [quota, setQuota] = useState<{ daily: boolean; message: string } | null>(null);
  const [cooldown, setCooldown] = useState(0);

  // 세션 시작은 진입 시 1회만 (StrictMode의 이펙트 이중 실행으로 대화가 두 개 발급되지 않게 막는다)
  const startedRef = useRef(false);

  const startSession = async () => {
    setStartError(null);
    try {
      const { conversationId: id } = await startEssaySession(question.id);
      setConversationId(id);
    } catch (e) {
      setStartError(
        e instanceof ApiError ? e.message : "세션을 시작하지 못했습니다. 다시 시도해주세요.",
      );
    }
  };

  useEffect(() => {
    // 비로그인이면 세션 시작 자체를 하지 않음
    if (startedRef.current || !loggedIn) return;
    startedRef.current = true;
    void startSession();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loggedIn]);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
    return () => clearTimeout(timer);
  }, [cooldown]);

  // 화면에 노출되는 문항 시퀀스 = 채점 완료된 문항들 + 답변 중 문항
  const seq = [...items.map((item) => item.question), ...(current ? [current] : [])];
  const idx = items.length;
  const done = current === null;
  const correctCount = items.filter((item) => item.isCorrect).length;
  const dailyQuotaReached = quota?.daily === true;

  const submit = async () => {
    if (!conversationId || !current || !draft.trim() || grading || cooldown > 0 || dailyQuotaReached) return;
    const answer = draft.trim();
    setGrading(true);
    setError(null);
    setQuota(null);
    try {
      const result = await evaluateEssayAnswer(question.id, {
        conversationId,
        question: current,
        answer,
      });
      setItems((prev) => [
        ...prev,
        {
          question: current,
          answer,
          feedback: result.grading.feedback,
          modelAnswer: result.grading.modelAnswer,
          isCorrect: result.grading.isCorrect,
          mastery: result.grading.mastery,
          masteryReason: result.grading.masteryReason,
        },
      ]);
      setCurrent(result.nextFollowup?.question ?? null);
      setDraft("");
    } catch (e) {
      // 실패해도 draft를 지우지 않는다 — 같은 답변으로 바로 재시도할 수 있게
      const code = e instanceof ApiError ? e.code : null;
      if (code === QUOTA_CODE || code === DAILY_QUOTA_CODE) {
        const daily = code === DAILY_QUOTA_CODE;
        setQuota({ daily, message: (e as ApiError).message });
        setCooldown(daily ? 0 : QUOTA_COOLDOWN_SECONDS);
      } else {
        setError(e instanceof ApiError ? e.message : "채점에 실패했습니다. 다시 시도해주세요.");
      }
    } finally {
      setGrading(false);
    }
  };

  const save = async () => {
    if (saving || items.length === 0) return;
    setSaving(true);
    setError(null);
    // 본질문만 questionId를 갖고, AI가 생성한 꼬리질문은 재사용 Question이 없어 null
    const toRequest = (item: GradedItem, i: number): EssaySolvedQuestionRequest => ({
      questionId: i === 0 ? question.id : null,
      questionText: item.question,
      userAnswer: item.answer,
      feedback: item.feedback,
      modelAnswer: item.modelAnswer,
      isCorrect: item.isCorrect,
    });
    try {
      await saveEssaySolvedSession({
        rootQuestion: toRequest(items[0], 0),
        followupQuestions: items.slice(1).map((item, i) => toRequest(item, i + 1)),
        startedAt,
      });
      onFinish(correctCount, items.length);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "저장에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setSaving(false);
    }
  };

  const toggle = (i: number) =>
    setOpen((o) => (o.includes(i) ? o.filter((x) => x !== i) : [...o, i]));

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-3.5">
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
        <SaveToProblemSetButton saved={saved} onClick={onOpenSaveModal} />
      </div>

      <div className="flex items-start gap-[18px]">
        {/* LEFT : 질문 목록 */}
        <Card className="min-w-0 flex-1 overflow-hidden">
          <CardHeader className="gap-2.5">
            <Badge tone="ai">서술형</Badge>
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
                    {label(i)}
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
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-[5px] bg-ink text-[10px] font-bold text-white">AI</span>
              <span className="text-[13px] font-bold text-secondary">AI 면접관에게 답하듯 작성하세요</span>
            </CardHeader>
            <div className="flex flex-col gap-3.5 px-[22px] py-5">
              {/* 답변 완료 기록 */}
              {items.map((e, i) => (
                <div key={i} className="overflow-hidden rounded-[12px] border border-line-card">
                  <div className="flex items-center gap-1.5 border-b border-line-soft bg-subtle px-4 py-[9px]">
                    <span className="inline-flex items-center rounded-[5px] bg-neutral px-2 py-0.5 text-[10.5px] font-bold text-secondary">
                      {label(i)} · 내 답변
                    </span>
                    <span
                      className={`ml-auto text-[12px] font-bold ${
                        e.isCorrect ? "text-success" : "text-danger"
                      }`}
                    >
                      {e.isCorrect ? "✓ 통과" : "✕ 미통과"}
                    </span>
                  </div>
                  <div className="whitespace-pre-wrap px-4 py-[13px] text-[14px] leading-[1.65] text-body">
                    {e.answer}
                  </div>
                  <button
                    type="button"
                    onClick={() => toggle(i)}
                    className="flex w-full items-center justify-between gap-2 border-t border-line-soft bg-white px-4 py-2.5 text-[12.5px] font-semibold text-accent"
                  >
                    <span className="flex items-center gap-1.5">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M12 2a10 10 0 100 20 10 10 0 000-20z" />
                        <path d="M12 16v-4M12 8h.01" />
                      </svg>
                      {open.includes(i) ? "AI 피드백 숨기기" : "AI 피드백 보기"}
                    </span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                      <path d={open.includes(i) ? "M18 15l-6-6-6 6" : "M6 9l6 6 6-6"} />
                    </svg>
                  </button>
                  {open.includes(i) && (
                    <div className="border-t border-line-soft bg-paper px-4 py-3.5">
                      <div className="mb-1.5 text-[11.5px] font-bold text-alert">AI 피드백</div>
                      <div className="text-[13.5px] leading-[1.65] text-alert-deep">{e.feedback}</div>
                      <MasteryVerdict mastery={e.mastery} masteryReason={e.masteryReason} />
                    </div>
                  )}
                </div>
              ))}

              {/* 채점 대기 — 채점 결과가 들어올 자리에 그대로 표시한다 */}
              {grading && <GradingProgress />}

              {/* 세션 시작 실패 — 답변 입력을 막고 재시도만 노출 */}
              {startError && (
                <div className="flex flex-col items-start gap-2.5 rounded-[12px] border border-alert-line bg-alert-bg px-4 py-3.5">
                  <div className="text-[13.5px] font-semibold text-alert-deep">{startError}</div>
                  <Button variant="secondary" onClick={startSession}>
                    다시 시도
                  </Button>
                </div>
              )}

              {/* AI 채점 쿼터 초과 — 분당 한도는 쿨다운 후 재시도, 일일 한도는 재시도 대신 이탈 안내 */}
              {quota && (
                <div className="flex flex-col items-start gap-2 rounded-[12px] border border-alert-line bg-alert-bg px-4 py-3.5">
                  <div className="text-[13.5px] font-semibold text-alert-deep">{quota.message}</div>
                  {quota.daily ? (
                    <>
                      <div className="text-[12.5px] text-alert-deep">
                        지금은 객관식 문제로 학습을 이어갈 수 있어요.
                      </div>
                      <Button variant="secondary" onClick={onQuit}>
                        문제은행으로 가기
                      </Button>
                    </>
                  ) : (
                    <div className="text-[12.5px] font-semibold text-alert-deep">
                      {cooldown > 0
                        ? `${cooldown}초 후 같은 답변으로 다시 제출할 수 있어요.`
                        : "이제 다시 제출할 수 있어요."}
                    </div>
                  )}
                </div>
              )}

              {/* 현재 질문 답변 입력 */}
              {!done && !startError && (
                <div className="flex flex-col gap-[11px]">
                  <div className="flex items-center gap-1.5">
                    <Badge tone="accent" size="xs">
                      {label(idx)}
                    </Badge>
                    <span className="text-xs font-semibold text-soft">
                      {!loggedIn
                        ? "· 로그인하면 답변할 수 있어요"
                        : conversationId
                          ? "에 답변해 주세요"
                          : "· 세션을 시작하는 중입니다…"}
                    </span>
                  </div>
                  <textarea
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                    onFocus={() => {
                      if (!loggedIn) setShowLoginGate(true);
                    }}
                    readOnly={!loggedIn}
                    disabled={loggedIn && (!conversationId || grading)}
                    placeholder="면접관에게 답하듯 설명해 보세요. 근거와 예시를 함께 들면 좋습니다."
                    className="block min-h-[170px] w-full resize-y rounded-[12px] border border-line-input bg-white px-4 py-3.5 text-[14.5px] leading-[1.7] text-ink outline-none disabled:bg-subtle disabled:text-soft"
                  />
                  <span className="font-mono text-xs text-placeholder">{draft.length}자</span>
                </div>
              )}

              {/* 미완주 상태 — 저장 조건(3문항 완주)을 못 채우면 진행분이 남지 않는다 */}
              {!done && items.length > 0 && (
                <div className="text-[12.5px] font-semibold text-soft">
                  3문항을 모두 답해야 저장할 수 있어요. 지금 종료하면 지금까지 답변은 저장되지 않습니다.
                </div>
              )}

              {/* 면접 완료 */}
              {done && (
                <div className="flex flex-col gap-2 border-t border-dashed border-line pt-4">
                  <div className="flex items-center gap-1.5 text-[15px] font-bold text-success">
                    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 6L9 17l-5-5" />
                    </svg>
                    면접 완료 · 꼬리질문까지 모두 답했어요
                  </div>
                  <div className="text-[13px] text-soft">
                    각 답변의 <b>AI 피드백 보기</b>를 눌러 보완할 점을 확인해 보세요.
                  </div>
                  {correctCount < items.length && (
                    <div className="text-[13px] font-semibold text-danger">
                      미통과 문항이 있어 저장 시 오답노트에 자동 저장됩니다.
                    </div>
                  )}
                </div>
              )}
            </div>
            <div className="flex items-center justify-end gap-2 border-t border-line-card px-[22px] py-3.5">
              {error ? (
                <span className="mr-auto text-[13px] font-semibold text-danger">{error}</span>
              ) : (
                done && (
                  <span className="mr-auto text-[13.5px] font-bold text-secondary">
                    통과 {correctCount} / {items.length}
                  </span>
                )
              )}
              {done ? (
                /* 마지막 문항까지 답했을 때만 세션 저장 */
                <Button size="lg" onClick={save} disabled={saving}>
                  {saving ? "저장 중…" : "저장하기"}
                </Button>
              ) : (
                <>
                  {/* 중도 이탈: 저장하지 않고 문제은행으로 복귀 */}
                  <Button variant="muted" size="lg" onClick={onQuit}>
                    종료하기
                  </Button>
                  <Button
                    size="lg"
                    onClick={submit}
                    disabled={
                      !conversationId || !draft.trim() || grading || cooldown > 0 || dailyQuotaReached
                    }
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
                </>
              )}
            </div>
          </Card>
        </div>
      </div>

      {showLoginGate && <LoginRequiredGate onClose={() => setShowLoginGate(false)} />}
    </div>
  );
}
