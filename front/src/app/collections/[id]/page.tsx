"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import type { ProblemSetDetailResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { deleteProblemSet, fetchProblemSetDetail, removeProblemSetItem } from "@/lib/problemSets";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProblemSetDetail from "@/components/problemSets/ProblemSetDetail";

// 문제집 상세 페이지 — 담긴 문제를 순서대로 풀거나 제거한다
export default function CollectionDetailPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();
  const problemSetId = Number(id);

  const [problemSet, setProblemSet] = useState<ProblemSetDetailResponse | null>(null);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchProblemSetDetail(problemSetId)
      .then((data) => {
        if (!cancelled) setProblemSet(data);
      })
      .catch((e) => {
        if (cancelled) return;
        if (e instanceof ApiError && e.status === 404) {
          setNotFound(true);
        } else {
          setError(e instanceof ApiError ? e.message : "문제집을 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [problemSetId]);

  const backToList = () => router.push("/collections");
  const startQuestion = (questionId: number) => router.push(`/solve/${questionId}`);

  const removeItem = (questionId: number) => {
    if (!problemSet) return;
    setProblemSet({ ...problemSet, items: problemSet.items.filter((item) => item.questionId !== questionId) });
    removeProblemSetItem(problemSet.id, questionId).catch(() => {
      setError("문제를 빼지 못했습니다. 새로고침 후 다시 시도해주세요.");
    });
  };

  const remove = async () => {
    if (!problemSet) return;
    try {
      await deleteProblemSet(problemSet.id);
      backToList();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "문제집을 삭제하지 못했습니다.");
    }
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="문제집" subtitle="담아둔 문제를 순서대로 풀어보세요" />
      <PageBody>
        {error && (
          <div className="max-w-[620px] rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-danger">
            {error}
          </div>
        )}

        {!error && notFound && (
          <div className="flex max-w-[620px] flex-col items-center gap-4 rounded-[16px] border border-line-card bg-white px-[22px] py-10 text-center text-[13.5px] text-soft">
            문제집을 찾을 수 없습니다
            <button
              type="button"
              onClick={backToList}
              className="rounded-[11px] bg-ink px-4 py-2.5 text-[14px] font-semibold text-white transition-colors hover:bg-ink-hover"
            >
              문제집 목록으로
            </button>
          </div>
        )}

        {!error && !notFound && !problemSet && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">문제집을 불러오는 중…</div>
        )}

        {!error && problemSet && (
          <ProblemSetDetail
            problemSet={problemSet}
            onBack={backToList}
            onDelete={remove}
            onRemoveItem={removeItem}
            onStartQuestion={startQuestion}
            onGoToSolve={() => router.push("/solve")}
          />
        )}
      </PageBody>
    </main>
  );
}
