"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type { WrongNoteDetailResponse, WrongNoteSummaryResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { deleteWrongNote, fetchWrongNoteDetail, fetchWrongNotes, updateWrongNoteBookmark } from "@/lib/wrongNotes";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";
import WrongNoteCard from "@/components/wrong/WrongNoteCard";
import WrongDetail from "@/components/wrong/WrongDetail";

const FILTERS = ["전체", "북마크"] as const;
type Filter = (typeof FILTERS)[number];

export default function WrongPage() {
  const router = useRouter();
  const [filter, setFilter] = useState<Filter>("전체");
  const [detailId, setDetailId] = useState<number | null>(null);

  // 현재 필터 조합의 조회 결과. key가 현재 필터와 다르면 아직 로딩 중
  const [listResult, setListResult] = useState<{
    key: Filter;
    list?: WrongNoteSummaryResponse[];
    error?: string;
  } | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchWrongNotes(filter === "북마크" ? true : undefined)
      .then((list) => {
        if (!cancelled) setListResult({ key: filter, list });
      })
      .catch((e) => {
        if (!cancelled) {
          setListResult({
            key: filter,
            error: e instanceof ApiError ? e.message : "오답노트를 불러오지 못했습니다.",
          });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [filter]);

  const listLoading = listResult?.key !== filter;
  const notes = (!listLoading && listResult?.list) || [];
  const listError = !listLoading ? (listResult?.error ?? null) : null;

  // 선택한 오답노트 상세 조회 결과. key가 현재 detailId와 다르면 아직 로딩 중
  const [detailResult, setDetailResult] = useState<{
    key: number;
    data?: WrongNoteDetailResponse;
    error?: string;
  } | null>(null);

  useEffect(() => {
    if (detailId === null) return;
    let cancelled = false;
    fetchWrongNoteDetail(detailId)
      .then((data) => {
        if (!cancelled) setDetailResult({ key: detailId, data });
      })
      .catch((e) => {
        if (!cancelled) {
          setDetailResult({
            key: detailId,
            error: e instanceof ApiError ? e.message : "오답노트를 불러오지 못했습니다.",
          });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [detailId]);

  const detailLoading = detailId !== null && detailResult?.key !== detailId;
  const detail = detailId !== null && !detailLoading ? (detailResult?.data ?? null) : null;
  const detailError = detailId !== null && !detailLoading ? (detailResult?.error ?? null) : null;

  const toggleBookmark = async (id: number, current: boolean) => {
    try {
      const result = await updateWrongNoteBookmark(id, !current);
      setListResult((r) =>
        r ? { ...r, list: r.list?.map((n) => (n.id === id ? { ...n, isBookmarked: result.isBookmarked } : n)) } : r,
      );
      setDetailResult((r) =>
        r && r.data && r.data.id === id ? { ...r, data: { ...r.data, isBookmarked: result.isBookmarked } } : r,
      );
    } catch {
      // 실패해도 조용히 무시 — 화면 상태는 그대로 유지된다
    }
  };

  const remove = async (id: number) => {
    try {
      await deleteWrongNote(id);
      setListResult((r) => (r ? { ...r, list: r.list?.filter((n) => n.id !== id) } : r));
    } catch {
      // 실패해도 조용히 무시 — 카드가 그대로 남아 재시도할 수 있다
    }
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader
        title={detailId === null ? "오답노트" : "오답 상세"}
        subtitle={detailId === null ? "틀린 문제를 자동 저장하고 반복 복습하세요" : "내가 푼 문제와 선택한 답을 다시 확인하세요"}
      />
      <PageBody>
        {detailId === null ? (
          <div className="flex flex-col gap-[18px]">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap gap-1.5">
                {FILTERS.map((f) => (
                  <Chip key={f} label={f} active={filter === f} onClick={() => setFilter(f)} />
                ))}
              </div>
            </div>

            {listLoading && (
              <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
                오답노트를 불러오는 중…
              </div>
            )}

            {!listLoading && listError && (
              <div className="px-[22px] py-10 text-center text-[13.5px] text-danger">{listError}</div>
            )}

            {!listLoading && !listError && notes.length === 0 && (
              <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
                {filter === "북마크" ? "북마크한 오답노트가 없습니다" : "오답노트가 없습니다"}
              </div>
            )}

            <div className="flex flex-col gap-2.5">
              {!listLoading &&
                !listError &&
                notes.map((note) => (
                  <WrongNoteCard
                    key={note.id}
                    note={note}
                    onOpen={() => setDetailId(note.id)}
                    onToggleBookmark={() => toggleBookmark(note.id, note.isBookmarked)}
                    onDelete={() => remove(note.id)}
                    onResolve={() => router.push("/solve")}
                  />
                ))}
            </div>
          </div>
        ) : (
          <>
            {detailLoading && (
              <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
                오답노트를 불러오는 중…
              </div>
            )}
            {!detailLoading && detailError && (
              <div className="px-[22px] py-10 text-center text-[13.5px] text-danger">{detailError}</div>
            )}
            {!detailLoading && detail && (
              <WrongDetail
                note={detail}
                onToggleBookmark={() => toggleBookmark(detail.id, detail.isBookmarked)}
                onBack={() => setDetailId(null)}
              />
            )}
          </>
        )}
      </PageBody>
    </main>
  );
}
