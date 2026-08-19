"use client";

import { useParams, useRouter } from "next/navigation";
import { adminQuestionView } from "@/mocks/admin";
import QuestionBasicInfo from "@/components/admin/QuestionBasicInfo";
import QuestionStats from "@/components/admin/QuestionStats";

export default function AdminQuestionDetailPage() {
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

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <QuestionBasicInfo
        question={question}
        onEdit={() => router.push(`/admin/questions/${question.id}/edit`)}
      />
      <QuestionStats question={question} />
    </div>
  );
}
