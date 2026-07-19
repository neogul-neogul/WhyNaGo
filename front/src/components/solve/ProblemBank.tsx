"use client";

import { useMemo, useState } from "react";
import type { Problem } from "@/types";
import { problemBank } from "@/mocks/problemBank";
import { CATEGORIES, diffColor, lvBadge } from "@/lib/badges";
import Chip from "@/components/ui/Chip";

const statusMeta: Record<Problem["status"], { c: string; bg: string }> = {
  완료: { c: "#16A34A", bg: "#F2FBF5" },
  오답: { c: "#DC2626", bg: "#FEF2F2" },
  "안 푼 문제": { c: "#9A9A90", bg: "#F1F1ED" },
};

const typeMeta: Record<Problem["type"], { c: string; bg: string }> = {
  객관식: { c: "#4F46E5", bg: "#EEF0FF" },
  서술형: { c: "#6D28D9", bg: "#F0EDFF" },
};

// 문제은행 목록 (검색 + 필터 + 표)
export default function ProblemBank({
  onStart,
}: {
  onStart: (p: Problem) => void;
}) {
  const [type, setType] = useState("전체");
  const [diff, setDiff] = useState("전체");
  const [cat, setCat] = useState("전체");
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return problemBank.filter(
      (p) =>
        (type === "전체" || p.type === type) &&
        (diff === "전체" || p.diff === diff) &&
        (cat === "전체" || p.cat === cat) &&
        (!q ||
          p.title.toLowerCase().includes(q) ||
          p.cat.toLowerCase().includes(q)),
    );
  }, [type, diff, cat, search]);

  return (
    <div className="flex max-w-[1000px] flex-col gap-4">
      {/* 검색 */}
      <div className="relative flex items-center">
        <span className="pointer-events-none absolute left-4 flex">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#A8A8A0" strokeWidth="2" strokeLinecap="round">
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4-4" />
          </svg>
        </span>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="풀고 싶은 문제 제목, 개념을 검색하세요"
          className="w-full rounded-[12px] border border-[#E0E0DA] bg-white py-[14px] pl-[46px] pr-4 text-[14.5px] text-[#1C1C1A] outline-none placeholder:text-[#A8A8A0]"
        />
      </div>

      {/* 필터 */}
      <div className="flex flex-col gap-[11px]">
        <FilterRow label="유형">
          {["전체", "객관식", "서술형"].map((t) => (
            <Chip key={t} label={t} active={type === t} onClick={() => setType(t)} />
          ))}
        </FilterRow>
        <FilterRow label="난이도">
          {["전체", "하", "중", "상"].map((d) => (
            <Chip key={d} label={d} active={diff === d} onClick={() => setDiff(d)} />
          ))}
        </FilterRow>
        <FilterRow label="카테고리" alignTop>
          {CATEGORIES.map((c) => (
            <Chip key={c} label={c} active={cat === c} onClick={() => setCat(c)} />
          ))}
        </FilterRow>
      </div>

      {/* 개수 + 정렬 */}
      <div className="flex items-center justify-between pt-1">
        <span className="text-[14px] font-semibold text-[#1C1C1A]">
          <span className="font-mono">{filtered.length}</span>개 문제
        </span>
        <span className="flex items-center gap-1.5 text-[13px] font-medium text-[#9A9A90]">
          최신순
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M6 9l6 6 6-6" />
          </svg>
        </span>
      </div>

      {/* 표 */}
      <div className="overflow-hidden rounded-[16px] border border-[#ECECE8] bg-white">
        <div className="flex items-center gap-4 border-b border-[#ECECE8] bg-[#FAFAF7] px-[22px] py-[13px] text-xs font-semibold text-[#A8A89E]">
          <span className="w-[84px] flex-shrink-0">상태</span>
          <span className="min-w-0 flex-1">제목</span>
          <span className="w-[70px] flex-shrink-0 text-center">유형</span>
          <span className="w-[60px] flex-shrink-0 text-center">난이도</span>
          <span className="w-[92px] flex-shrink-0 text-right">완료한 사람</span>
          <span className="w-[60px] flex-shrink-0 text-right">정답률</span>
        </div>
        {filtered.map((p, i) => {
          const sm = statusMeta[p.status];
          const tm = typeMeta[p.type];
          return (
            <button
              key={`${p.type}-${p.qi}-${i}`}
              type="button"
              onClick={() => onStart(p)}
              className="flex w-full items-center gap-4 border-b border-[#F2F2EE] bg-white px-[22px] py-[15px] text-left transition-colors hover:bg-[#FAFAF7]"
            >
              <span className="w-[84px] flex-shrink-0">
                <span
                  className="inline-flex items-center whitespace-nowrap rounded-[6px] px-2.5 py-1 text-[11.5px] font-semibold"
                  style={{ color: sm.c, background: sm.bg }}
                >
                  {p.status}
                </span>
              </span>
              <span className="flex min-w-0 flex-1 flex-col gap-[3px]">
                <span className="truncate text-[14.5px] font-semibold text-[#1C1C1A]">
                  {p.title}
                </span>
                <span className="flex flex-wrap gap-1.5">
                  {p.keywords.map((kw) => (
                    <span key={kw} className="whitespace-nowrap rounded-[5px] bg-white px-1 py-0.5 text-[10px] font-medium text-[#6B6B62]">
                      {kw}
                    </span>
                  ))}
                </span>
              </span>
              <span className="flex w-[70px] flex-shrink-0 justify-center">
                <span
                  className="whitespace-nowrap rounded-[6px] px-[9px] py-[3px] text-[11.5px] font-semibold"
                  style={{ color: tm.c, background: tm.bg }}
                >
                  {p.type}
                </span>
              </span>
              <span
                className="w-[60px] flex-shrink-0 text-center text-[12.5px] font-bold"
                style={{ color: diffColor(p.diff) }}
              >
                {lvBadge(p.diff)}
              </span>
              <span className="w-[92px] flex-shrink-0 text-right font-mono text-[13px] text-[#6B6B62]">
                {p.solved.toLocaleString()}명
              </span>
              <span className="w-[60px] flex-shrink-0 text-right font-mono text-[13px] font-semibold text-[#6B6B62]">
                {p.rate}%
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

function FilterRow({
  label,
  alignTop,
  children,
}: {
  label: string;
  alignTop?: boolean;
  children: React.ReactNode;
}) {
  return (
    <div className={`flex flex-wrap gap-3 ${alignTop ? "items-start" : "items-center"}`}>
      <span
        className={`w-[52px] flex-shrink-0 text-[12.5px] font-semibold text-[#A8A89E] ${alignTop ? "pt-2" : ""}`}
      >
        {label}
      </span>
      <div className="flex flex-1 flex-wrap gap-2">{children}</div>
    </div>
  );
}
