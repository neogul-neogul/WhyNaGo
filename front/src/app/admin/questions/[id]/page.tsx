"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import type { AdminQuestionDetailResponse, MultipleChoiceStatisticsResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchAdminQuestionDetail, fetchAdminQuestionStatistics } from "@/lib/admin";
import QuestionBasicInfo from "@/components/admin/QuestionBasicInfo";
import QuestionStats from "@/components/admin/QuestionStats";

export default function AdminQuestionDetailPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();
  const questionId = Number(id);

  // key가 지금 보고 있는 문제와 다르면 아직 로딩 중
  const [result, setResult] = useState<{
    key: number;
    question?: AdminQuestionDetailResponse;
    statistics?: MultipleChoiceStatisticsResponse | null;
    error?: string;
  } | null>(null);

  useEffect(() => {
    if (!Number.isInteger(questionId)) return;
    let cancelled = false;

    // 통계는 객관식에만 있는 API라 유형을 확인한 뒤 이어서 조회한다
    fetchAdminQuestionDetail(questionId)
      .then(async (question) => {
        const statistics =
          question.type === "MULTIPLE_CHOICE"
            ? await fetchAdminQuestionStatistics(questionId)
            : null;
        if (!cancelled) setResult({ key: questionId, question, statistics });
      })
      .catch((e) => {
        if (!cancelled) {
          setResult({
            key: questionId,
            error: e instanceof ApiError ? e.message : "문제를 불러오지 못했습니다.",
          });
        }
      });

    return () => {
      cancelled = true;
    };
  }, [questionId]);

  if (!Number.isInteger(questionId)) {
    return <Message tone="soft">문제를 찾을 수 없습니다.</Message>;
  }

  const loading = result?.key !== questionId;
  if (loading) {
    return <Message tone="soft">문제를 불러오는 중…</Message>;
  }
  if (result?.error) {
    return <Message tone="danger">{result.error}</Message>;
  }

  const question = result?.question;
  if (!question) {
    return <Message tone="soft">문제를 찾을 수 없습니다.</Message>;
  }

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <QuestionBasicInfo
        question={question}
        onEdit={() => router.push(`/admin/questions/${question.id}/edit`)}
      />
      <QuestionStats question={question} statistics={result?.statistics ?? null} />
    </div>
  );
}

function Message({ tone, children }: { tone: "soft" | "danger"; children: React.ReactNode }) {
  return (
    <div
      className={`px-[22px] py-10 text-center text-[13.5px] ${
        tone === "danger" ? "text-danger" : "text-soft"
      }`}
    >
      {children}
    </div>
  );
}
