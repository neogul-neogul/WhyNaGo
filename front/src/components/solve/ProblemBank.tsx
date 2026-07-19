"use client";

import { useMemo, useState } from "react";
import type { Problem } from "@/types";
import { problemBank } from "@/mocks/problemBank";
import { CATEGORIES, diffColor, lvBadge } from "@/lib/badges";
import Chip from "@/components/ui/Chip";
import Badge, { type BadgeTone } from "@/components/ui/Badge";

const statusTone: Record<Problem["status"], BadgeTone> = {
  완료: "success",
  오답: "danger",
  "안 푼 문제": "neutral",
};

const typeTone: Record<Problem["type"], BadgeTone> = {
  객관식: "accent",
  서술형: "ai",
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
        <span className="pointer-events-none absolute left-4 flex text-placeholder">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4-4" />
          </svg>
        </span>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="풀고 싶은 문제 제목, 개념을 검색하세요"
          className="w-full rounded-[12px] border border-line-input bg-white py-[14px] pl-[46px] pr-4 text-[14.5px] text-ink outline-none placeholder:text-placeholder"
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
        <span className="text-[14px] font-semibold text-ink">
          <span className="font-mono">{filtered.length}</span>개 문제
        </span>
        <span className="flex items-center gap-1.5 text-[13px] font-medium text-soft">
          최신순
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M6 9l6 6 6-6" />
          </svg>
        </span>
      </div>

      {/* 표 */}
      <div className="overflow-hidden rounded-[16px] border border-line-card bg-white">
        <div className="flex items-center gap-4 border-b border-line-card bg-subtle px-[22px] py-[13px] text-xs font-semibold text-placeholder">
          <span className="w-[84px] flex-shrink-0">상태</span>
          <span className="min-w-0 flex-1">제목</span>
          <span className="w-[70px] flex-shrink-0 text-center">유형</span>
          <span className="w-[60px] flex-shrink-0 text-center">난이도</span>
          <span className="w-[92px] flex-shrink-0 text-right">완료한 사람</span>
          <span className="w-[60px] flex-shrink-0 text-right">정답률</span>
        </div>
        {filtered.map((p, i) => (
          <button
            key={`${p.type}-${p.qi}-${i}`}
            type="button"
            onClick={() => onStart(p)}
            className="flex w-full items-center gap-4 border-b border-line-soft bg-white px-[22px] py-[15px] text-left transition-colors hover:bg-subtle"
          >
            <span className="w-[84px] flex-shrink-0">
              <Badge tone={statusTone[p.status]}>{p.status}</Badge>
            </span>
            <span className="flex min-w-0 flex-1 flex-col gap-[3px]">
              <span className="truncate text-[14.5px] font-semibold text-ink">
                {p.title}
              </span>
              <span className="flex flex-wrap gap-1.5">
                {p.keywords.map((kw) => (
                  <span key={kw} className="whitespace-nowrap rounded-[5px] bg-white px-1 py-0.5 text-[10px] font-medium text-secondary">
                    {kw}
                  </span>
                ))}
              </span>
            </span>
            <span className="flex w-[70px] flex-shrink-0 justify-center">
              <Badge tone={typeTone[p.type]}>{p.type}</Badge>
            </span>
            <span
              className="w-[60px] flex-shrink-0 text-center text-[12.5px] font-bold"
              style={{ color: diffColor(p.diff) }}
            >
              {lvBadge(p.diff)}
            </span>
            <span className="w-[92px] flex-shrink-0 text-right font-mono text-[13px] text-secondary">
              {p.solved.toLocaleString()}명
            </span>
            <span className="w-[60px] flex-shrink-0 text-right font-mono text-[13px] font-semibold text-secondary">
              {p.rate}%
            </span>
          </button>
        ))}
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
        className={`w-[52px] flex-shrink-0 text-[12.5px] font-semibold text-placeholder ${alignTop ? "pt-2" : ""}`}
      >
        {label}
      </span>
      <div className="flex flex-1 flex-wrap gap-2">{children}</div>
    </div>
  );
}
