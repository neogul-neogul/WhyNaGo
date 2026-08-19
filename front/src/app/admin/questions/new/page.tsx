"use client";

import { useRouter } from "next/navigation";
import QuestionForm from "@/components/admin/QuestionForm";

const EMPTY_OPTIONS = Array.from({ length: 4 }, () => ({ text: "", explanation: "" }));

export default function AdminQuestionNewPage() {
  const router = useRouter();

  return (
    <QuestionForm
      initial={{
        category: "NETWORK",
        difficulty: "MEDIUM",
        tags: [],
        title: "",
        body: "",
        explanation: "",
        answerIndex: 0,
        options: EMPTY_OPTIONS,
      }}
      onCancel={() => router.push("/admin/questions")}
    />
  );
}
