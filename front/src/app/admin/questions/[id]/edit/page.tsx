"use client";

import { useParams, useRouter } from "next/navigation";
import { adminQuestionFormSeed, adminQuestionView } from "@/mocks/admin";
import QuestionForm from "@/components/admin/QuestionForm";

export default function AdminQuestionEditPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();

  const question = adminQuestionView(id);
  if (!question) {
    return (
      <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
        문제를 찾을 수 없습니다.
      </div>
    );
  }

  // 시안에 편집용 보기·해설 더미가 준비된 문제가 하나뿐이라, 나머지는 목록 값으로 폼을 채운다
  const initial =
    question.type === "서술형"
      ? {
          category: question.category,
          difficulty: question.difficulty,
          tags: question.detail.tags,
          title: question.detail.title,
          body: question.detail.body,
          modelAnswer: question.detail.answerExplanation ?? "",
        }
      : {
          ...adminQuestionFormSeed,
          category: question.category,
          difficulty: question.difficulty,
          tags: question.detail.tags,
          title: question.detail.title,
          body: question.detail.body,
          explanation: question.detail.answerExplanation ?? "",
        };

  return (
    <QuestionForm
      type={question.type}
      initial={initial}
      onCancel={() => router.push(`/admin/questions/${question.id}`)}
    />
  );
}
