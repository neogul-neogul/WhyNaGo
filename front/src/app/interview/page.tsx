"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import type { StartInterviewResponse, TodayInterviewResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { useAuth, useHydrated } from "@/lib/auth";
import {
  fetchTodayInterview,
  INTERVIEW_ALREADY_STARTED_TODAY,
  INTERVIEW_TIME_LIMIT_FALLBACK,
  INTERVIEW_TOTAL_QUESTION_FALLBACK,
  startInterview,
} from "@/lib/interviews";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import LoginRequiredGate from "@/components/layout/LoginRequiredGate";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import InterviewIntro from "@/components/interview/InterviewIntro";
import InterviewSession from "@/components/interview/InterviewSession";
import InterviewStartConfirmModal from "@/components/interview/InterviewStartConfirmModal";

/**
 * 1일 1면접 안내 + 진행.
 *
 * 진행 화면을 별도 라우트로 빼지 않는다 — 새로고침하면 어느 쪽이든 진행 상태가 유실되므로
 * 라우트를 늘리는 대신 진행 중 `beforeunload` 경고로 막는다.
 */
export default function InterviewPage() {
  const router = useRouter();
  const hydrated = useHydrated();
  const loggedIn = useAuth();

  const [today, setToday] = useState<TodayInterviewResponse | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [started, setStarted] = useState<StartInterviewResponse | null>(null);
  const [starting, setStarting] = useState(false);
  const [startError, setStartError] = useState<string | null>(null);
  const [showLoginGate, setShowLoginGate] = useState(false);
  const [showStartConfirm, setShowStartConfirm] = useState(false);
  const startingRef = useRef(false);

  // 상태 갱신은 응답 콜백 안에서만 한다 — 이펙트 본문에서 동기 setState를 하지 않기 위해서다
  const loadToday = () =>
    fetchTodayInterview()
      .then((data) => {
        setToday(data);
        setLoadError(null);
      })
      .catch((e) => {
        setLoadError(e instanceof ApiError ? e.message : "면접 상태를 불러오지 못했습니다.");
      });

  useEffect(() => {
    if (!hydrated || !loggedIn) return;
    void loadToday();
  }, [hydrated, loggedIn]);

  const start = async () => {
    if (startingRef.current) return;
    startingRef.current = true;
    setStarting(true);
    setStartError(null);
    try {
      setStarted(await startInterview());
    } catch (e) {
      // 다른 탭에서 이미 시작했을 수 있다 — 상태를 다시 조회해 화면을 정정한다
      if (e instanceof ApiError && e.code === INTERVIEW_ALREADY_STARTED_TODAY) {
        await loadToday();
      } else {
        setStartError(
          e instanceof ApiError ? e.message : "면접을 시작하지 못했습니다. 다시 시도해주세요.",
        );
      }
    } finally {
      startingRef.current = false;
      setStarting(false);
    }
  };

  /** 한 문항도 채점받지 못해 취소됨 — 오늘 자리는 돌아왔다 */
  const handleAborted = (message: string) => {
    setStarted(null);
    setStartError(message);
    void loadToday();
  };

  const body = () => {
    if (!hydrated) return <Placeholder text="불러오는 중…" />;

    // 비로그인은 안내 화면까지는 보여주고, "면접 진행" 클릭 시에만 로그인을 요구한다
    if (!loggedIn) {
      return (
        <>
          <InterviewIntro onStart={() => setShowLoginGate(true)} starting={false} error={null} />
          {showLoginGate && <LoginRequiredGate onClose={() => setShowLoginGate(false)} />}
        </>
      );
    }

    // 면접 진행 중 (이 세션에서 시작한 경우에만 진행 화면을 띄운다)
    if (started) {
      return (
        <InterviewSession
          interviewId={started.interviewId}
          question={started.question}
          totalQuestionCount={started.totalQuestionCount || INTERVIEW_TOTAL_QUESTION_FALLBACK}
          timeLimitSeconds={started.timeLimitSeconds || INTERVIEW_TIME_LIMIT_FALLBACK}
          onCompleted={() => router.push(`/interview/result/${started.interviewId}`)}
          onAborted={handleAborted}
        />
      );
    }

    if (loadError) {
      return (
        <Card className="flex flex-col items-start gap-3 p-7">
          <div className="text-[13.5px] font-semibold text-danger">{loadError}</div>
          <Button variant="secondary" onClick={() => void loadToday()}>
            다시 시도
          </Button>
        </Card>
      );
    }

    if (!today) return <Placeholder text="오늘의 면접 상태를 확인하는 중…" />;

    if (today.status === "COMPLETED") {
      return (
        <Card className="flex flex-col items-start gap-3 p-7">
          <div className="text-[16px] font-bold text-ink">오늘의 면접을 마쳤어요</div>
          <div className="text-[13.5px] leading-[1.7] text-soft">
            면접은 하루에 한 번만 볼 수 있어요. 내일 새로운 질문으로 다시 만나요.
          </div>
          <div className="flex gap-2">
            <Link
              href={`/interview/result/${today.interviewId}`}
              className="rounded-[11px] bg-ink px-7 py-[13px] text-[15px] font-semibold text-white transition-colors hover:bg-ink-hover"
            >
              결과 보기
            </Link>
            <Link
              href="/interview/history"
              className="rounded-[11px] border border-line-strong bg-white px-7 py-[13px] text-[15px] font-semibold text-ink transition-colors hover:border-ink"
            >
              면접 기록 보기
            </Link>
          </div>
        </Card>
      );
    }

    if (today.status === "IN_PROGRESS") {
      return (
        <Card className="flex flex-col items-start gap-3 p-7">
          <div className="text-[16px] font-bold text-ink">
            면접이 완료되지 않은 채 종료되었어요
          </div>
          <div className="text-[13.5px] leading-[1.7] text-soft">
            새로고침하거나 창을 닫으면 진행 중이던 면접을 이어갈 수 없어요. 오늘 자리는 이미
            사용되어 내일 다시 도전할 수 있습니다.
          </div>
          <div className="flex gap-2">
            <Link
              href="/solve"
              className="rounded-[11px] bg-ink px-7 py-[13px] text-[15px] font-semibold text-white transition-colors hover:bg-ink-hover"
            >
              문제 풀이로 가기
            </Link>
            <Link
              href="/"
              className="rounded-[11px] border border-line-strong bg-white px-7 py-[13px] text-[15px] font-semibold text-ink transition-colors hover:border-ink"
            >
              홈으로
            </Link>
          </div>
        </Card>
      );
    }

    return (
      <>
        <InterviewIntro onStart={() => setShowStartConfirm(true)} starting={starting} error={startError} />
        {showStartConfirm && (
          <InterviewStartConfirmModal
            onConfirm={() => {
              setShowStartConfirm(false);
              void start();
            }}
            onClose={() => setShowStartConfirm(false)}
          />
        )}
      </>
    );
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="1일 1면접" subtitle="AI와 함께하는 하루 한 번의 모의 면접" />
      <PageBody>
        {/* 진행 화면은 2단 레이아웃이라 폭을 제한하지 않는다. 안내 화면은 문제 풀이(ProblemBank)와 같은 폭 */}
        <div className={`flex flex-col gap-[18px] ${started ? "" : "mx-auto max-w-[1000px]"}`}>
          {body()}
        </div>
      </PageBody>
    </main>
  );
}

function Placeholder({ text }: { text: string }) {
  return <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">{text}</div>;
}
