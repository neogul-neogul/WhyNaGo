"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { AdminMember } from "@/types";
import { ADMIN_TIERS, adminMembers } from "@/mocks/admin";
import Input from "@/components/ui/Input";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import { TierBadge } from "@/components/admin/AdminBadges";
import FilterSelect from "@/components/admin/FilterSelect";

const PAGE_SIZE = 8;
const TIER_OPTIONS = ["전체", ...ADMIN_TIERS];

const COLUMNS: AdminColumn<AdminMember>[] = [
  {
    key: "id",
    header: "ID",
    width: 74,
    render: (m) => <span className="font-mono text-[13px] font-medium text-secondary">{m.id}</span>,
  },
  {
    key: "nickname",
    header: "닉네임",
    width: 132,
    render: (m) => <span className="text-sm font-semibold text-ink">{m.nickname}</span>,
  },
  {
    key: "email",
    header: "이메일",
    render: (m) => <span className="text-[13.5px] font-medium text-secondary">{m.email}</span>,
  },
  {
    key: "position",
    header: "포지션",
    width: 90,
    render: (m) => <span className="text-[13.5px] font-medium text-secondary">{m.position}</span>,
  },
  {
    key: "tier",
    header: "티어",
    width: 100,
    render: (m) => <TierBadge tier={m.tier} />,
  },
  {
    key: "joinedAt",
    header: "가입일",
    width: 96,
    align: "right",
    render: (m) => (
      <span className="font-mono text-[13px] font-medium text-secondary">{m.joinedAt}</span>
    ),
  },
];

export default function AdminMembersPage() {
  const router = useRouter();
  const [tier, setTier] = useState("전체");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);

  const keyword = query.trim().toLowerCase();
  const filtered = adminMembers.filter(
    (m) =>
      (tier === "전체" || m.tier === tier) &&
      (!keyword ||
        m.nickname.toLowerCase().includes(keyword) ||
        m.email.toLowerCase().includes(keyword)),
  );

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, totalPages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, (current + 1) * PAGE_SIZE);

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <FilterSelect
          label="티어"
          value={tier}
          options={TIER_OPTIONS}
          onChange={(v) => {
            setTier(v);
            setPage(0);
          }}
        />

        <div className="relative min-w-0 flex-1">
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            className="absolute left-4 top-1/2 -translate-y-1/2 text-placeholder"
          >
            <circle cx="11" cy="11" r="7" />
            <path d="M16.5 16.5L21 21" strokeLinecap="round" />
          </svg>
          <Input
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setPage(0);
            }}
            placeholder="닉네임 · 이메일 검색"
            className="pl-[46px]"
          />
        </div>

        <button
          type="button"
          onClick={() => {
            setTier("전체");
            setQuery("");
            setPage(0);
          }}
          className="cursor-pointer px-1 text-[13.5px] font-semibold text-secondary transition-colors hover:text-ink"
        >
          초기화
        </button>
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={rows}
        rowKey={(m) => m.id}
        onRowClick={(m) => router.push(`/admin/members/${m.id}`)}
        emptyText="조건에 맞는 회원이 없습니다."
      />

      <Pagination page={current} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}
