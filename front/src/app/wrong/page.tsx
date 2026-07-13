"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { wrongData } from "@/mocks/wrong";
import { diffColor } from "@/lib/badges";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Chip from "@/components/ui/Chip";
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
                <div
                  key={i}
                  onClick={() => setDetailIdx(i)}
                  className="flex cursor-pointer flex-col gap-3 rounded-[13px] border border-[#ECECE8] bg-white px-5 py-[18px] transition-all hover:border-[#D4D4CC] hover:shadow-[0_2px_10px_rgba(0,0,0,0.04)]"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex flex-1 items-start gap-2.5">
                      <button
                        type="button"
                        title="북마크"
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleBookmark(i);
                        }}
                        className="flex flex-shrink-0 items-center justify-center p-0.5"
                        style={{ color: bookmarked.includes(i) ? "#4F46E5" : "#C4C4BC" }}
                      >
                        <svg width="17" height="17" viewBox="0 0 24 24" fill={bookmarked.includes(i) ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
                        </svg>
                      </button>
                      <div className="flex-1 text-[15px] font-semibold leading-[1.5] text-[#1C1C1A]">{w.q}</div>
                    </div>
                    <button
                      type="button"
                      title="오답노트에서 삭제"
                      onClick={(e) => {
                        e.stopPropagation();
                        setDeleted((d) => [...d, i]);
                      }}
                      className="flex flex-shrink-0 items-center justify-center rounded-[6px] p-1 text-[#B0B0A6] transition-colors hover:bg-[#F1F1ED] hover:text-[#DC2626]"
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M18 6L6 18M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded-[6px] bg-[#F1F1ED] px-[9px] py-[3px] text-xs font-medium text-[#6B6B62]">{w.cat}</span>
                      <span className="text-xs font-semibold" style={{ color: diffColor(w.diff) }}>난이도 {w.diff}</span>
                    </div>
                    <div className="flex items-center gap-2.5">
                      <span className="text-xs font-medium text-[#A8A8A0]">{w.solvedAt}</span>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          router.push("/solve");
                        }}
                        className="rounded-[8px] bg-[#1C1C1A] px-3.5 py-1.5 text-[12.5px] font-semibold text-white"
                      >
                        재풀이
                      </button>
                    </div>
                  </div>
                </div>
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
