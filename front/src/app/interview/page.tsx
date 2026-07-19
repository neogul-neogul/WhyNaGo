"use client";

import { useState } from "react";
import { interviewBank } from "@/mocks/interview";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import AiNotice from "@/components/interview/AiNotice";
import InterviewIntro from "@/components/interview/InterviewIntro";
import InterviewAnswer from "@/components/interview/InterviewAnswer";
import InterviewFeedback from "@/components/interview/InterviewFeedback";

type Stage = "intro" | "answering" | "feedback";

export default function InterviewPage() {
  const [stage, setStage] = useState<Stage>("intro");
  const [cat, setCat] = useState("네트워크");
  const [answer, setAnswer] = useState("");

  const iv = interviewBank[cat] ?? interviewBank["전체"];

  const reset = () => {
    setStage("intro");
    setAnswer("");
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="1일 1면접" subtitle="AI와 함께하는 하루 한 번의 모의 면접" />
      <PageBody>
        <div className="flex max-w-[780px] flex-col gap-[18px]">
          <AiNotice />

          {stage === "intro" && (
            <InterviewIntro
              cat={cat}
              onSelectCat={setCat}
              onStart={() => {
                setAnswer("");
                setStage("answering");
              }}
            />
          )}

          {stage === "answering" && (
            <InterviewAnswer
              cat={cat}
              question={iv.q}
              answer={answer}
              onChangeAnswer={setAnswer}
              onSubmit={() => setStage("feedback")}
            />
          )}

          {stage === "feedback" && (
            <InterviewFeedback
              cat={cat}
              interview={iv}
              answer={answer}
              onRestart={reset}
              onSave={reset}
            />
          )}
        </div>
      </PageBody>
    </main>
  );
}
