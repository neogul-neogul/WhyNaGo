"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import type { AdminQuestionDetailResponse, AdminQuestionForm } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchAdminQuestionDetail } from "@/lib/admin";
import QuestionForm from "@/components/admin/QuestionForm";

// 문제 수정 화면. 폼 값은 실제 문제를 불러와 채우지만, 저장 API가 아직 없어 저장은 나가지 않는다.
export default function AdminQuestionEditPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();
  const questionId = Number(id);

  // key가 지금 보고 있는 문제와 다르면 아직 로딩 중
  const [result, setResult] = useState<{
    key: number;
    question?: AdminQuestionDetailResponse;
    error?: string;
  } | null>(null);

  useEffect(() => {
    if (!Number.isInteger(questionId)) return;
    let cancelled = false;
    fetchAdminQuestionDetail(questionId)
      .then((question) => {
        if (!cancelled) setResult({ key: questionId, question });
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
    <QuestionForm
      type={question.type}
      initial={toForm(question)}
      onCancel={() => router.push(`/admin/questions/${question.id}`)}
    />
  );
}

// 서술형은 보기가 없어 모범답안만, 객관식은 해설 + 보기별 오답 해설을 채운다
function toForm(question: AdminQuestionDetailResponse): AdminQuestionForm {
  const base = {
    category: question.category,
    difficulty: question.difficulty,
    tags: question.tags,
    title: question.title,
    body: question.content,
  };

  if (question.type === "ESSAY") {
    return { ...base, modelAnswer: question.explanation ?? "" };
  }

  return {
    ...base,
    explanation: question.explanation ?? "",
    answerIndex: question.choices.findIndex((choice) => choice.correct),
    options: question.choices.map((choice) => ({
      text: choice.content,
      explanation: choice.explanation ?? "",
    })),
  };
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
