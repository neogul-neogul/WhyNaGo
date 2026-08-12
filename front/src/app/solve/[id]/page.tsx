"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import type { ProblemSetMembershipResponse, QuestionResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchQuestions } from "@/lib/questions";
import { fetchProblemSetMembership } from "@/lib/problemSets";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import MultipleChoiceQuiz from "@/components/solve/MultipleChoiceQuiz";
import EssayQuiz from "@/components/solve/EssayQuiz";
import QuizResult from "@/components/solve/QuizResult";
import SaveToProblemSetModal from "@/components/problemSets/SaveToProblemSetModal";

type Stage = "quiz" | "result";

// 문제 상세(풀이) 페이지 — GET /api/questions는 id 단건 조회가 없어
// 목록을 조회한 뒤 해당 id의 문항을 찾는다
export default function SolveDetailPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();

  const [question, setQuestion] = useState<QuestionResponse | null>(null);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [stage, setStage] = useState<Stage>("quiz");
  const [result, setResult] = useState({ correct: 0, total: 0 });
  const [saveModalOpen, setSaveModalOpen] = useState(false);
  const [membership, setMembership] = useState<ProblemSetMembershipResponse[]>([]);
  const saved = membership.some((m) => m.saved);

  useEffect(() => {
    let cancelled = false;
    fetchQuestions()
      .then((list) => {
        if (cancelled) return;
        const found = list.find((q) => String(q.id) === id) ?? null;
        setQuestion(found);
        setNotFound(!found);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "문제를 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  useEffect(() => {
    if (!question) return;
    let cancelled = false;
    fetchProblemSetMembership(question.id)
      .then((list) => {
        if (!cancelled) setMembership(list);
      })
      .catch(() => {
        // 저장 여부를 못 가져와도 풀이 자체는 계속할 수 있어야 하므로 조용히 무시한다
      });
    return () => {
      cancelled = true;
    };
  }, [question]);

  const backToList = () => router.push("/solve");

  const finish = (correct: number, total: number) => {
    setResult({ correct, total });
    setStage("result");
  };

  const isMultipleChoice = question?.type === "MULTIPLE_CHOICE";

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader
        title="문제 풀이"
        subtitle="문제은행에서 풀고 싶은 문제를 골라 바로 도전하세요"
      />
      <PageBody>
        {error && (
          <div className="max-w-[620px] rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-danger">
            {error}
          </div>
        )}

        {!error && notFound && (
          <div className="flex max-w-[620px] flex-col items-center gap-4 rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-soft">
            문제를 찾을 수 없습니다
            <button
              type="button"
              onClick={backToList}
              className="rounded-[11px] bg-ink px-4 py-2.5 text-[14px] font-semibold text-white transition-colors hover:bg-ink-hover"
            >
              문제 목록으로
            </button>
          </div>
        )}

        {!error && !notFound && !question && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
            문제를 불러오는 중…
          </div>
        )}

        {question && stage === "quiz" && (
          isMultipleChoice ? (
            <MultipleChoiceQuiz
              key={`mc-${question.id}`}
              question={question}
              onQuit={backToList}
              onFinish={finish}
              saved={saved}
              onOpenSaveModal={() => setSaveModalOpen(true)}
            />
          ) : (
            <EssayQuiz
              key={`essay-${question.id}`}
              question={question}
              onQuit={backToList}
              onFinish={finish}
              saved={saved}
              onOpenSaveModal={() => setSaveModalOpen(true)}
            />
          )
        )}

        {question && stage === "result" && (
          <QuizResult
            type={isMultipleChoice ? "객관식" : "서술형"}
            correct={result.correct}
            total={result.total}
            onRestart={backToList}
          />
        )}

        {question && (
          <SaveToProblemSetModal
            open={saveModalOpen}
            onClose={() => setSaveModalOpen(false)}
            questionId={question.id}
            questionTitle={question.title}
            membership={membership}
            onMembershipChange={setMembership}
          />
        )}
      </PageBody>
    </main>
  );
}
