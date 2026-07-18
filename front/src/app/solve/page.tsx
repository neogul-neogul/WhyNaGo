"use client";

import { useState } from "react";
import type { Problem } from "@/types";
import { multipleChoiceQuestions, essayQuestions } from "@/mocks/questions";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProblemBank from "@/components/solve/ProblemBank";
import MultipleChoiceQuiz from "@/components/solve/MultipleChoiceQuiz";
import EssayQuiz from "@/components/solve/EssayQuiz";
import QuizResult from "@/components/solve/QuizResult";

type Stage = "setup" | "quiz" | "result";

export default function SolvePage() {
  const [stage, setStage] = useState<Stage>("setup");
  const [type, setType] = useState<Problem["type"]>("객관식");
  const [qi, setQi] = useState(0);
  const [result, setResult] = useState({ correct: 0, total: 0 });

  const startProblem = (p: Problem) => {
    setType(p.type);
    setQi(p.qi);
    setStage("quiz");
  };

  const finish = (correct: number, total: number) => {
    setResult({ correct, total });
    setStage("result");
  };

  const multipleChoice = multipleChoiceQuestions[qi % multipleChoiceQuestions.length];
  const essay = essayQuestions[qi % essayQuestions.length];

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader
        title="문제 풀이"
        subtitle="문제은행에서 풀고 싶은 문제를 골라 바로 도전하세요"
      />
      <PageBody>
        {stage === "setup" && <ProblemBank onStart={startProblem} />}

        {stage === "quiz" &&
          (type === "객관식" ? (
            <MultipleChoiceQuiz
              key={`mc-${qi}`}
              question={multipleChoice}
              onQuit={() => setStage("setup")}
              onFinish={finish}
            />
          ) : (
            <EssayQuiz
              key={`essay-${qi}`}
              question={essay}
              onQuit={() => setStage("setup")}
            />
          ))}

        {stage === "result" && (
          <QuizResult
            type={type}
            correct={result.correct}
            total={result.total}
            onRestart={() => setStage("setup")}
          />
        )}
      </PageBody>
    </main>
  );
}
