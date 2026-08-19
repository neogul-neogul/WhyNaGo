"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { adminQuestionView } from "@/mocks/admin";
import QuestionBasicInfo from "@/components/admin/QuestionBasicInfo";
import QuestionStats from "@/components/admin/QuestionStats";

const TABS = [
  { key: "basic", label: "기본 정보" },
  { key: "stats", label: "통계" },
] as const;

type TabKey = (typeof TABS)[number]["key"];

export default function AdminQuestionDetailPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();
  const [tab, setTab] = useState<TabKey>("basic");

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
      <div className="flex items-center gap-[22px] border-b border-line">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            onClick={() => setTab(t.key)}
            className={`-mb-px cursor-pointer border-b-2 px-0.5 pb-3 text-sm transition-colors ${
              tab === t.key
                ? "border-ink font-bold text-ink"
                : "border-transparent font-medium text-soft hover:text-ink"
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === "basic" ? (
        <QuestionBasicInfo
          question={question}
          onEdit={() => router.push(`/admin/questions/${question.id}/edit`)}
        />
      ) : (
        <QuestionStats question={question} />
      )}
    </div>
  );
}
