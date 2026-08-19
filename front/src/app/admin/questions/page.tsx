"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { AdminQuestion } from "@/types";
import { ADMIN_CATEGORIES, ADMIN_DIFFICULTIES, adminQuestions } from "@/mocks/admin";
import Input from "@/components/ui/Input";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import { CategoryBadge, DifficultyText, StatusBadge } from "@/components/admin/AdminBadges";
import FilterSelect from "@/components/admin/FilterSelect";

const PAGE_SIZE = 8;
const CATEGORY_OPTIONS = ["전체", ...ADMIN_CATEGORIES];
const DIFFICULTY_OPTIONS = ["전체", ...ADMIN_DIFFICULTIES];
const TYPE_OPTIONS = ["전체", "객관식", "서술형"];

const COLUMNS: AdminColumn<AdminQuestion>[] = [
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
    render: (q) => <span className="text-[13px] font-medium text-secondary">{q.type}</span>,
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
    render: (q) => <span className="font-mono text-[13.5px] font-semibold text-ink">{q.correctRate}</span>,
  },
  {
    key: "updatedAt",
    header: "수정일",
    width: 56,
    align: "right",
    render: (q) => (
      <span className="font-mono text-[13px] font-medium text-placeholder">{q.updatedAt}</span>
    ),
  },
  {
    key: "status",
    header: "상태",
    width: 96,
    align: "right",
    render: (q) => <StatusBadge status={q.status} />,
  },
];

export default function AdminQuestionsPage() {
  const router = useRouter();
  const [category, setCategory] = useState("전체");
  const [difficulty, setDifficulty] = useState("전체");
  const [type, setType] = useState("전체");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);

  const keyword = query.trim().toLowerCase();
  const filtered = adminQuestions.filter(
    (q) =>
      (category === "전체" || q.category === category) &&
      (difficulty === "전체" || q.difficulty === difficulty) &&
      (type === "전체" || q.type === type) &&
      (!keyword ||
        q.title.toLowerCase().includes(keyword) ||
        q.category.toLowerCase().includes(keyword)),
  );

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, totalPages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, (current + 1) * PAGE_SIZE);

  const reset = <T,>(setter: (v: T) => void) => (value: T) => {
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
          onChange={(e) => {
            setQuery(e.target.value);
            setPage(0);
          }}
          placeholder="문제 제목 · 태그 검색"
          className="min-w-[220px] flex-1"
        />
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={rows}
        rowKey={(q) => q.id}
        onRowClick={(q) => router.push(`/admin/questions/${q.id}`)}
        emptyText="조건에 맞는 문제가 없습니다."
      />

      <Pagination page={current} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}
