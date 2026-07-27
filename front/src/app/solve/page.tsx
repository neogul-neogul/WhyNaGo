"use client";

import { useState } from "react";
import type { QuestionResponse } from "@/types";
import { essayQuestions } from "@/mocks/questions";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProblemBank from "@/components/solve/ProblemBank";
import MultipleChoiceQuiz from "@/components/solve/MultipleChoiceQuiz";
import EssayQuiz from "@/components/solve/EssayQuiz";
import QuizResult from "@/components/solve/QuizResult";

type Stage = "setup" | "quiz" | "result";

export default function SolvePage() {
  const [stage, setStage] = useState<Stage>("setup");
  const [question, setQuestion] = useState<QuestionResponse | null>(null);
  const [result, setResult] = useState({ correct: 0, total: 0 });

  const startProblem = (q: QuestionResponse) => {
    setQuestion(q);
    setStage("quiz");
  };

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
        {stage === "setup" && <ProblemBank onStart={startProblem} />}

        {stage === "quiz" &&
          question &&
          (isMultipleChoice ? (
            <MultipleChoiceQuiz
              key={`mc-${question.id}`}
              question={question}
              onQuit={() => setStage("setup")}
              onFinish={finish}
            />
          ) : (
            /* 서술형은 아직 백엔드 미구현 — 더미 데이터 화면 유지 */
            <EssayQuiz
              key={`essay-${question.id}`}
              question={essayQuestions[0]}
              onQuit={() => setStage("setup")}
            />
          ))}

        {stage === "result" && (
          <QuizResult
            type={isMultipleChoice ? "객관식" : "서술형"}
            correct={result.correct}
            total={result.total}
            onRestart={() => setStage("setup")}
          />
        )}
      </PageBody>
    </main>
  );
}
