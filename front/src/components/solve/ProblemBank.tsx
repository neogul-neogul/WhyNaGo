"use client";

import { useEffect, useState } from "react";
import type { PageResponse, QuestionResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { useCurrentUser, useHydrated } from "@/lib/auth";
import {
  CATEGORY_LABELS,
  DIFFICULTY_LABELS,
  QUESTION_PAGE_SIZE,
  TYPE_LABELS,
  categoryFromLabel,
  difficultyFromLabel,
  fetchQuestions,
  typeFromLabel,
} from "@/lib/questions";
import { CATEGORIES, diffColor, lvBadge } from "@/lib/badges";
import Chip from "@/components/ui/Chip";
import Badge, { type BadgeTone } from "@/components/ui/Badge";
import Pagination from "@/components/ui/Pagination";

const typeTone: Record<string, BadgeTone> = {
  객관식: "accent",
  서술형: "ai",
};

// 문제은행 목록 (검색 + 필터 + 표) — GET /api/questions
export default function ProblemBank({
  onStart,
}: {
  onStart: (question: QuestionResponse) => void;
}) {
  const [type, setType] = useState("전체");
  const [diff, setDiff] = useState("전체");
  const [cat, setCat] = useState("전체");
  const [search, setSearch] = useState("");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const userId = useCurrentUser()?.id ?? null;
  const hydrated = useHydrated();

  // 현재 조회 조합의 결과. key가 지금 조합과 다르면 아직 로딩 중
  // 푼 문제 표시(solved)가 사용자마다 다르므로 userId도 조합에 넣는다
  const filtersKey = `${type}|${diff}|${cat}|${keyword}|${userId}|${page}`;
  const [result, setResult] = useState<{
    key: string;
    page?: PageResponse<QuestionResponse>;
    error?: string;
  } | null>(null);

  // 검색어는 잠시 멈췄을 때만 서버에 반영
  useEffect(() => {
    const timer = setTimeout(() => {
      setKeyword(search.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  // hydration 전에는 userId가 항상 null이라, 여기서 조회하면 확정 후 한 번 더 나간다
  useEffect(() => {
    if (!hydrated) return;
    let cancelled = false;
    fetchQuestions(
      {
        type: typeFromLabel(type),
        difficulty: difficultyFromLabel(diff),
        category: categoryFromLabel(cat),
        keyword: keyword || undefined,
      },
      { page, size: QUESTION_PAGE_SIZE },
    )
      .then((response) => {
        if (!cancelled) setResult({ key: filtersKey, page: response });
      })
      .catch((e) => {
        if (!cancelled) {
          setResult({
            key: filtersKey,
            error: e instanceof ApiError ? e.message : "문제 목록을 불러오지 못했습니다.",
          });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [type, diff, cat, keyword, userId, hydrated, page, filtersKey]);

  const loading = result?.key !== filtersKey;
  const questions = (!loading && result?.page?.content) || [];
  const error = !loading ? (result?.error ?? null) : null;
  const totalElements = result?.page?.totalElements ?? 0;
  const totalPages = result?.page?.totalPages ?? 0;

  const movePage = (next: number) => {
    setPage(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const changeFilter = (apply: (value: string) => void, value: string) => {
    apply(value);
    setPage(0);
  };

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
            <Chip key={t} label={t} active={type === t} onClick={() => changeFilter(setType, t)} />
          ))}
        </FilterRow>
        <FilterRow label="난이도">
          {["전체", "하", "중", "상"].map((d) => (
            <Chip key={d} label={d} active={diff === d} onClick={() => changeFilter(setDiff, d)} />
          ))}
        </FilterRow>
        <FilterRow label="카테고리" alignTop>
          {CATEGORIES.map((c) => (
            <Chip key={c} label={c} active={cat === c} onClick={() => changeFilter(setCat, c)} />
          ))}
        </FilterRow>
      </div>

      {/* 개수 + 정렬 */}
      <div className="flex items-center justify-between pt-1">
        <span className="text-[14px] font-semibold text-ink">
          <span className="font-mono">{totalElements}</span>개 문제
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
          <span className="min-w-0 flex-1">제목</span>
          <span className="w-[70px] flex-shrink-0 text-center">유형</span>
          <span className="w-[80px] flex-shrink-0 text-center">카테고리</span>
          <span className="w-[60px] flex-shrink-0 text-center">난이도</span>
        </div>

        {loading && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
            문제 목록을 불러오는 중…
          </div>
        )}

        {!loading && error && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-danger">
            {error}
          </div>
        )}

        {!loading && !error && questions.length === 0 && (
          <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
            조건에 맞는 문제가 없습니다
          </div>
        )}

        {!loading &&
          !error &&
          questions.map((q) => {
            const diffLabel = DIFFICULTY_LABELS[q.difficulty];
            const typeLabel = TYPE_LABELS[q.type];
            return (
              <button
                key={q.id}
                type="button"
                onClick={() => onStart(q)}
                className="flex w-full items-center gap-4 border-b border-line-soft bg-white px-[22px] py-[15px] text-left transition-colors hover:bg-subtle"
              >
                <span className="flex min-w-0 flex-1 items-start gap-2">
                  <SolvedMark solved={q.solved} />
                  <span className="flex min-w-0 flex-1 flex-col gap-[3px]">
                    <span className="truncate text-[14.5px] font-semibold text-ink">
                      {q.title}
                    </span>
                    <span className="flex flex-wrap gap-1.5">
                      {q.tags.map((tag) => (
                        <span key={tag} className="whitespace-nowrap rounded-[5px] bg-white px-1 py-0.5 text-[10px] font-medium text-secondary">
                          {tag}
                        </span>
                      ))}
                    </span>
                  </span>
                </span>
                <span className="flex w-[70px] flex-shrink-0 justify-center">
                  <Badge tone={typeTone[typeLabel] ?? "neutral"}>{typeLabel}</Badge>
                </span>
                <span className="w-[80px] flex-shrink-0 text-center text-[12.5px] font-medium text-secondary">
                  {CATEGORY_LABELS[q.category]}
                </span>
                <span
                  className="w-[60px] flex-shrink-0 text-center text-[12.5px] font-bold"
                  style={{ color: diffColor(diffLabel) }}
                >
                  {lvBadge(diffLabel)}
                </span>
              </button>
            );
          })}
      </div>

      {!error && <Pagination page={page} totalPages={totalPages} onChange={movePage} />}
    </div>
  );
}

// 안 푼 문제도 같은 크기의 자리를 차지해 제목 시작 위치를 맞춘다
function SolvedMark({ solved }: { solved: boolean }) {
  if (!solved) {
    return <span aria-hidden className="mt-[3px] h-[15px] w-[15px] flex-shrink-0" />;
  }
  return (
    <span
      title="푼 문제"
      className="mt-[3px] flex h-[15px] w-[15px] flex-shrink-0 items-center justify-center rounded-full bg-success"
    >
      <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round">
        <path d="M5 13l4 4L19 7" />
      </svg>
    </span>
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
