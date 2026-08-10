"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { InterviewResultResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { useAuth, useHydrated } from "@/lib/auth";
import {
  fetchInterviewResult,
  INTERVIEW_NOT_COMPLETED,
  INTERVIEW_NOT_FOUND,
} from "@/lib/interviews";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import LoginRequiredGate from "@/components/layout/LoginRequiredGate";
import Card from "@/components/ui/Card";
import InterviewResult from "@/components/interview/InterviewResult";

// 면접 결과 페이지 — 완료 직후 이동 + 나중에 다시 방문
export default function InterviewResultPage() {
  const { id } = useParams<{ id: string }>();
  const hydrated = useHydrated();
  const loggedIn = useAuth();

  const [result, setResult] = useState<InterviewResultResponse | null>(null);
  const [notice, setNotice] = useState<{ title: string; body: string } | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 잘못된 주소는 state가 아니라 렌더 시점에 바로 판단한다 (이펙트 동기 setState 회피)
  const interviewId = Number(id);
  const invalidId = !Number.isFinite(interviewId);

  useEffect(() => {
    if (!hydrated || !loggedIn || invalidId) return;

    let cancelled = false;
    fetchInterviewResult(interviewId)
      .then((data) => {
        if (!cancelled) setResult(data);
      })
      .catch((e) => {
        if (cancelled) return;
        const code = e instanceof ApiError ? e.code : null;
        if (code === INTERVIEW_NOT_FOUND) {
          setNotice({
            title: "면접을 찾을 수 없어요",
            body: "삭제되었거나 접근할 수 없는 면접입니다.",
          });
        } else if (code === INTERVIEW_NOT_COMPLETED) {
          setNotice({
            title: "완료되지 않은 면접이에요",
            body: "면접이 끝까지 진행되지 않아 결과가 없습니다. 내일 다시 도전할 수 있어요.",
          });
        } else {
          setError(e instanceof ApiError ? e.message : "결과를 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [hydrated, loggedIn, interviewId, invalidId]);

  const body = () => {
    if (!hydrated) return <Placeholder text="불러오는 중…" />;
    if (!loggedIn) return <LoginRequiredGate />;

    const shown = invalidId
      ? { title: "면접을 찾을 수 없어요", body: "잘못된 주소예요." }
      : notice;

    if (shown) {
      return (
        <Card className="flex flex-col items-start gap-3 p-7">
          <div className="text-[16px] font-bold text-ink">{shown.title}</div>
          <div className="text-[13.5px] leading-[1.7] text-soft">{shown.body}</div>
          <div className="flex gap-2">
            <Link
              href="/interview"
              className="rounded-[11px] bg-ink px-7 py-[13px] text-[15px] font-semibold text-white transition-colors hover:bg-ink-hover"
            >
              면접으로 돌아가기
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

    if (error) {
      return (
        <Card className="p-7 text-[13.5px] font-semibold text-danger">{error}</Card>
      );
    }

    if (!result) return <Placeholder text="결과를 불러오는 중…" />;

    return <InterviewResult result={result} />;
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="면접 결과" subtitle="오늘 면접에서 받은 피드백과 모범답안을 확인하세요" />
      <PageBody>
        <div className="mx-auto max-w-[860px]">{body()}</div>
      </PageBody>
    </main>
  );
}

function Placeholder({ text }: { text: string }) {
  return <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">{text}</div>;
}
