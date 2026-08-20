"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type { RecommendedQuestionResponse, WeakTagsResponse } from "@/types";
import { CATEGORY_LABELS, DIFFICULTY_LABELS, TYPE_LABELS } from "@/lib/questions";
import { fetchRecommendations, fetchWeakTags } from "@/lib/recommendations";
import { ApiError } from "@/lib/api";
import { SparkleIcon } from "@/components/today/RecommendationEntry";

type Phase = "idle" | "loading" | "done";

// 서버가 실제로 하는 일만 적는다. 오개념 추출은 구현되지 않았고, 없는 단계를 보여주면
// 사용자는 받은 문항이 그 분석의 결과라고 믿는다.
const LOAD_STEPS = ["취약 카테고리 분석 중", "취약 태그 선정 중", "문제 문장을 작성하는 중"];

/**
 * 맞춤 문제 추천의 idle → loading → done 상태를 관리한다.
 *
 * 서버는 한 번의 호출로 여러 문항을 내려주고, 같은 날 재호출하면 (약점 프로필이 그대로인 한)
 * 같은 문항을 같은 순서로 돌려준다. 그래서 "다른 문제"는 재호출이 아니라 받아둔 목록을
 * 순환해 보여준다 — 다시 부른들 새로 생성되지 않으므로 생성 연출을 반복할 이유가 없다.
 */
export default function RecommendationFlow() {
  const router = useRouter();
  const [phase, setPhase] = useState<Phase>("idle");
  const [step, setStep] = useState(0);
  const [questions, setQuestions] = useState<RecommendedQuestionResponse[]>([]);
  const [index, setIndex] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [weakTags, setWeakTags] = useState<WeakTagsResponse | null>(null);

  useEffect(() => {
    if (phase !== "loading") return;
    const second = window.setTimeout(() => setStep(1), 900);
    const third = window.setTimeout(() => setStep(2), 1800);
    return () => {
      window.clearTimeout(second);
      window.clearTimeout(third);
    };
  }, [phase]);

  useEffect(() => {
    let cancelled = false;
    fetchWeakTags()
      .then((result) => {
        if (!cancelled) setWeakTags(result);
      })
      .catch(() => {
        // 조회 실패도 추천 문항 생성은 막지 않는다.
        if (!cancelled) setWeakTags({ sampleCount: 0, tags: [] });
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const generate = async () => {
    if (phase === "loading") return;
    setPhase("loading");
    setStep(0);
    setError(null);

    try {
      const response = await fetchRecommendations();
      if (response.questions.length === 0) {
        throw new Error("추천할 문제를 찾지 못했습니다.");
      }
      setQuestions(response.questions);
      setIndex(0);
      setPhase("done");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "추천 문제를 불러오지 못했습니다.");
      setPhase("idle");
    }
  };

  // 받아둔 문항을 처음부터 끝까지 한 바퀴 돈다. 마지막 다음은 다시 첫 문항이다.
  const showNext = () => setIndex((current) => (current + 1) % questions.length);

  if (phase === "loading") {
    return <LoadingCard step={LOAD_STEPS[step]} />;
  }

  const question = questions[index];
  if (phase === "done" && question) {
    return (
      <GeneratedQuestionCard
        question={question}
        position={index + 1}
        total={questions.length}
        onSolve={() => router.push(`/solve/${question.id}`)}
        onNext={showNext}
      />
    );
  }

  return <WeakTagCard weakTags={weakTags} error={error} onGenerate={generate} />;
}

function WeakTagCard({ weakTags, error, onGenerate }: { weakTags: WeakTagsResponse | null; error: string | null; onGenerate: () => void }) {
  return (
    <section className="flex flex-col gap-[18px] rounded-[16px] border border-line-card bg-white px-[26px] py-6">
      <div className="flex items-baseline justify-between gap-4">
        <h2 className="text-[15.5px] font-semibold text-ink">취약 태그</h2>
        {weakTags && <span className="text-[12.5px] text-soft">전체 이력 · {weakTags.sampleCount}문제 기준</span>}
      </div>
      {!weakTags && <div className="rounded-[10px] bg-subtle px-4 py-3 text-[13px] leading-[1.6] text-muted">취약 태그를 분석하고 있어요.</div>}
      {weakTags && weakTags.tags.length === 0 && <div className="rounded-[10px] bg-subtle px-4 py-3 text-[13px] leading-[1.6] text-muted">학습 이력이 쌓이면 취약 태그를 분석합니다.</div>}
      {weakTags && weakTags.tags.length > 0 && (
        <div className="flex flex-col gap-[13px]">
          {weakTags.tags.map((tag) => (
            <div key={tag.tag} className="flex items-center gap-4">
              <span className="w-28 flex-shrink-0 rounded-[20px] bg-accent-bg px-2.5 py-1 text-center font-mono text-[13px] font-semibold text-accent">{tag.tag}</span>
              <div className="h-2 flex-1 overflow-hidden rounded-[20px] bg-neutral">
                <div className="h-full rounded-[20px] bg-alert" style={{ width: `${Math.round(tag.weaknessScore * 100)}%` }} />
              </div>
              <span className="w-[54px] flex-shrink-0 text-right font-mono text-[12.5px] text-secondary">{Math.round(tag.weaknessScore * 100)}%</span>
              <span className="w-20 flex-shrink-0 text-[12px] text-soft">풀이 {tag.sampleCount}문제</span>
            </div>
          ))}
        </div>
      )}
      <p className="text-[13px] leading-[1.6] text-muted">
        취약 태그를 바탕으로 오늘 풀 문제를 만들어 드립니다.
      </p>
      {error && <p className="text-[13px] text-danger">{error}</p>}
      <div className="border-t border-line-soft pt-[18px]">
        <button type="button" onClick={onGenerate} className="flex w-full items-center justify-center gap-[9px] rounded-[11px] bg-ink px-4 py-4 text-[14.5px] font-semibold text-white transition-colors hover:bg-ink-hover">
          <SparkleIcon className="h-[17px] w-[17px]" />
          취약점 기반 문제 받기
        </button>
      </div>
    </section>
  );
}

function LoadingCard({ step }: { step: string }) {
  return (
    <section className="flex flex-col items-center gap-5 rounded-[16px] border border-line-card bg-white px-6 py-[76px]">
      <span className="h-[42px] w-[42px] animate-spin rounded-full border-[3px] border-line-card border-t-accent" />
      <div className="flex flex-col items-center gap-[7px]">
        <h2 className="text-[15px] font-semibold text-ink">취약점에 맞는 문제를 만들고 있어요</h2>
        <span className="text-[13px] text-soft">{step}</span>
      </div>
    </section>
  );
}

function GeneratedQuestionCard({ question, position, total, onSolve, onNext }: { question: RecommendedQuestionResponse; position: number; total: number; onSolve: () => void; onNext: () => void }) {
  const tag = question.tags[0] ?? "맞춤 추천";
  return (
    <section className="flex flex-col gap-5 rounded-[16px] border border-line-card bg-white px-[30px] py-7">
      <div className="flex flex-wrap items-center gap-[9px]">
        <span className="rounded-[5px] bg-accent-bg px-2 py-[3px] text-[11px] font-bold text-accent">난이도 {DIFFICULTY_LABELS[question.difficulty]}</span>
        <span className="rounded-[20px] bg-accent-bg px-2 py-[3px] font-mono text-[12px] font-semibold text-accent">{tag}</span>
        <span className="text-[12.5px] text-muted">{CATEGORY_LABELS[question.category]}</span>
        <span className="text-line-strong">·</span>
        <span className="text-[12.5px] text-muted">{TYPE_LABELS[question.type]}</span>
        {total > 1 && <span className="ml-auto font-mono text-[12px] text-soft">{position} / {total}</span>}
      </div>
      <h2 className="text-[21px] font-semibold leading-[1.45] text-ink">{question.title}</h2>
      <p className="whitespace-pre-wrap text-[14.5px] leading-[1.7] text-body">{question.content}</p>
      <div className="flex items-center gap-2.5">
        <button type="button" onClick={onSolve} className="rounded-[10px] bg-ink px-[26px] py-[13px] text-[14px] font-semibold text-white transition-colors hover:bg-ink-hover">이 문제 풀기</button>
        {total > 1 && (
          <button type="button" onClick={onNext} className="flex items-center gap-2 rounded-[10px] border border-line-strong bg-white px-5 py-[13px] text-[14px] font-semibold text-ink transition-colors hover:border-ink">
            <RefreshIcon />
            다른 문제 보기
          </button>
        )}
      </div>
    </section>
  );
}

function RefreshIcon() {
  return <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12a9 9 0 11-3.5-7.1" /><path d="M21 3v6h-6" /></svg>;
}
