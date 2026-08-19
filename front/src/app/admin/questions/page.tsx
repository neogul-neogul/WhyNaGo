"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type { AdminQuestionResponse, PageResponse, QuestionCategory } from "@/types";
import { ApiError } from "@/lib/api";
import {
  ADMIN_CATEGORIES,
  ADMIN_DIFFICULTY_LABELS,
  ADMIN_QUESTION_PAGE_SIZE,
  ADMIN_QUESTION_TYPES,
  fetchAdminQuestions,
  formatRate,
} from "@/lib/admin";
import { TYPE_LABELS, difficultyFromLabel, typeFromLabel } from "@/lib/questions";
import { mockQuestionMeta } from "@/mocks/admin";
import Input from "@/components/ui/Input";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import { CategoryBadge, DifficultyText, StatusBadge } from "@/components/admin/AdminBadges";
import FilterSelect from "@/components/admin/FilterSelect";

const ALL = "전체";
const CATEGORY_OPTIONS = [ALL, ...ADMIN_CATEGORIES];
const DIFFICULTY_OPTIONS = [ALL, ...ADMIN_DIFFICULTY_LABELS];
const TYPE_OPTIONS = [ALL, ...ADMIN_QUESTION_TYPES.map((type) => TYPE_LABELS[type])];

const COLUMNS: AdminColumn<AdminQuestionResponse>[] = [
  { key: "category", header: "카테고리", width: 140, render: (q) => <CategoryBadge category={q.category} /> },
  {
    key: "difficulty",
    header: "난이도",
    width: 74,
    render: (q) => <DifficultyText difficulty={q.difficulty} />,
  },
  {
    key: "type",
    header: "유형",
    width: 60,
    render: (q) => (
      <span className="text-[13px] font-medium text-secondary">{TYPE_LABELS[q.type]}</span>
    ),
  },
  {
    key: "title",
    header: "문제 제목",
    render: (q) => <span className="block truncate text-sm font-semibold text-ink">{q.title}</span>,
  },
  {
    key: "solveCount",
    header: "풀이 횟수",
    width: 76,
    align: "right",
    render: (q) => (
      <span className="font-mono text-[13.5px] font-medium text-secondary">
        {q.solveCount.toLocaleString()}
      </span>
    ),
  },
  {
    key: "correctRate",
    header: "정답률",
    width: 64,
    align: "right",
    render: (q) => (
      <span className="font-mono text-[13.5px] font-semibold text-ink">
        {formatRate(q.correctRate)}
      </span>
    ),
  },
  // 수정일·상태는 Question에 컬럼이 없어 API가 내려주지 않는다. 목업 값이다.
  {
    key: "updatedAt",
    header: "수정일",
    width: 56,
    align: "right",
    render: (q) => (
      <span className="font-mono text-[13px] font-medium text-placeholder">
        {/* "2026-08-12 14:22" → "08-12" */}
        {mockQuestionMeta(q.id).updatedAt.slice(5, 10)}
      </span>
    ),
  },
  {
    key: "status",
    header: "상태",
    width: 96,
    align: "right",
    render: (q) => <StatusBadge status={mockQuestionMeta(q.id).status} />,
  },
];

export default function AdminQuestionsPage() {
  const router = useRouter();
  const [category, setCategory] = useState(ALL);
  const [difficulty, setDifficulty] = useState(ALL);
  const [type, setType] = useState(ALL);
  const [query, setQuery] = useState("");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);

  // 현재 조회 조합의 결과. key가 지금 조합과 다르면 아직 로딩 중
  const filtersKey = `${category}|${difficulty}|${type}|${keyword}|${page}`;
  const [result, setResult] = useState<{
    key: string;
    page?: PageResponse<AdminQuestionResponse>;
    error?: string;
  } | null>(null);

  // 검색어는 잠시 멈췄을 때만 서버에 반영
  useEffect(() => {
    const timer = setTimeout(() => {
      setKeyword(query.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    let cancelled = false;
    fetchAdminQuestions(
      {
        type: typeFromLabel(type),
        difficulty: difficultyFromLabel(difficulty),
        category: category === ALL ? undefined : (category as QuestionCategory),
        keyword: keyword || undefined,
      },
      { page, size: ADMIN_QUESTION_PAGE_SIZE },
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
  }, [category, difficulty, type, keyword, page, filtersKey]);

  const loading = result?.key !== filtersKey;
  const rows = (!loading && result?.page?.content) || [];
  const error = !loading ? (result?.error ?? null) : null;
  const totalPages = result?.page?.totalPages ?? 0;

  const emptyText = loading ? "문제 목록을 불러오는 중…" : (error ?? "조건에 맞는 문제가 없습니다.");

  const reset = (setter: (value: string) => void) => (value: string) => {
    setter(value);
    setPage(0);
  };

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center gap-[11px]">
        <FilterSelect label="카테고리" value={category} options={CATEGORY_OPTIONS} onChange={reset(setCategory)} />
        <FilterSelect label="난이도" value={difficulty} options={DIFFICULTY_OPTIONS} onChange={reset(setDifficulty)} />
        <FilterSelect label="유형" value={type} options={TYPE_OPTIONS} onChange={reset(setType)} />

        <Input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="문제 제목 · 태그 검색"
          className="min-w-[220px] flex-1"
        />
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={rows}
        rowKey={(q) => String(q.id)}
        onRowClick={(q) => router.push(`/admin/questions/${q.id}`)}
        emptyText={emptyText}
      />

      {!error && <Pagination page={page} totalPages={totalPages} onChange={setPage} />}
    </div>
  );
}
