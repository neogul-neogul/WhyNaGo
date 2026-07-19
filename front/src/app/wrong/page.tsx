"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { wrongData } from "@/mocks/wrong";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";
import WrongNoteCard from "@/components/wrong/WrongNoteCard";
import WrongDetail from "@/components/wrong/WrongDetail";

const FILTERS = ["전체", "미복습", "북마크"];

export default function WrongPage() {
  const router = useRouter();
  const [filter, setFilter] = useState("전체");
  const [deleted, setDeleted] = useState<number[]>([]);
  const [bookmarked, setBookmarked] = useState<number[]>([0]);
  const [detailIdx, setDetailIdx] = useState<number | null>(null);

  const toggleBookmark = (i: number) =>
    setBookmarked((b) => (b.includes(i) ? b.filter((x) => x !== i) : [...b, i]));

  const list = wrongData
    .map((w, i) => ({ w, i }))
    .filter(
      ({ w, i }) =>
        !deleted.includes(i) &&
        (filter === "전체" ||
          (filter === "북마크" ? bookmarked.includes(i) : w.status === filter)),
    );

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title={detailIdx === null ? "오답노트" : "오답 상세"} subtitle={detailIdx === null ? "틀린 문제를 자동 저장하고 반복 복습하세요" : "내가 푼 문제와 선택한 답을 다시 확인하세요"} />
      <PageBody>
        {detailIdx === null ? (
          <div className="flex flex-col gap-[18px]">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap gap-1.5">
                {FILTERS.map((f) => (
                  <Chip key={f} label={f} active={filter === f} onClick={() => setFilter(f)} />
                ))}
              </div>
            </div>

            <div className="flex flex-col gap-2.5">
              {list.map(({ w, i }) => (
                <WrongNoteCard
                  key={i}
                  note={w}
                  bookmarked={bookmarked.includes(i)}
                  onOpen={() => setDetailIdx(i)}
                  onToggleBookmark={() => toggleBookmark(i)}
                  onDelete={() => setDeleted((d) => [...d, i])}
                  onResolve={() => router.push("/solve")}
                />
              ))}
            </div>
          </div>
        ) : (
          <WrongDetail
            note={wrongData[detailIdx]}
            bookmarked={bookmarked.includes(detailIdx)}
            onToggleBookmark={() => toggleBookmark(detailIdx)}
            onBack={() => setDetailIdx(null)}
          />
        )}
      </PageBody>
    </main>
  );
}
