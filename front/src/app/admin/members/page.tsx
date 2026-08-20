"use client";

import { useEffect, useState } from "react";
import type { AdminMemberResponse, PageResponse } from "@/types";
import { ApiError } from "@/lib/api";
import {
  ADMIN_MEMBER_PAGE_SIZE,
  fetchAdminMemberSummary,
  fetchAdminMembers,
  formatJoinedDate,
} from "@/lib/admin";
import { ADMIN_TIERS, mockMemberMeta } from "@/mocks/admin";
import Input from "@/components/ui/Input";
import Pagination from "@/components/ui/Pagination";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import { AnomalyBadge, MemberStatusBadge, TierBadge } from "@/components/admin/AdminBadges";
import FilterSelect from "@/components/admin/FilterSelect";
import MemberDetailModal from "@/components/admin/MemberDetailModal";

const ALL = "전체";
const TIER_OPTIONS = [ALL, ...ADMIN_TIERS];

const COLUMNS: AdminColumn<AdminMemberResponse>[] = [
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
  // 티어는 점수 파생값이고 점수 컬럼이 없어 API가 내려주지 않는다. 목업 값이다.
  {
    key: "tier",
    header: "티어",
    width: 100,
    render: (m) => <TierBadge tier={mockMemberMeta(m.id).tier} />,
  },
  {
    key: "provider",
    header: "가입경로",
    width: 76,
    render: (m) => <span className="text-[12.5px] font-medium text-secondary">{m.provider}</span>,
  },
  {
    key: "createdAt",
    header: "가입일",
    width: 96,
    align: "right",
    render: (m) => (
      <span className="font-mono text-[13px] font-medium text-secondary">
        {formatJoinedDate(m.createdAt)}
      </span>
    ),
  },
  // 최근활동일·이상징후·상태는 판정할 데이터(방문 로그·AI 호출 로그·상태 컬럼)가 없다. 목업 값이다.
  {
    key: "lastVisitedAt",
    header: "최근활동일",
    width: 100,
    align: "right",
    render: (m) => (
      <span className="font-mono text-[13px] font-medium text-placeholder">
        {mockMemberMeta(m.id).lastVisitedAt}
      </span>
    ),
  },
  {
    key: "anomaly",
    header: "이상징후",
    width: 112,
    align: "right",
    render: (m) => <AnomalyBadge anomaly={mockMemberMeta(m.id).anomaly} />,
  },
  {
    key: "status",
    header: "상태",
    width: 76,
    align: "right",
    render: (m) => <MemberStatusBadge status={mockMemberMeta(m.id).status} />,
  },
];

export default function AdminMembersPage() {
  const [tier, setTier] = useState(ALL);
  const [query, setQuery] = useState("");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<AdminMemberResponse | null>(null);
  const [summary, setSummary] = useState<{ totalCount: number; activeWeekCount: number } | null>(
    null,
  );

  // 현재 조회 조합의 결과. key가 지금 조합과 다르면 아직 로딩 중
  const filtersKey = `${keyword}|${page}`;
  const [result, setResult] = useState<{
    key: string;
    page?: PageResponse<AdminMemberResponse>;
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
    fetchAdminMembers(keyword || undefined, { page, size: ADMIN_MEMBER_PAGE_SIZE })
      .then((response) => {
        if (!cancelled) setResult({ key: filtersKey, page: response });
      })
      .catch((e) => {
        if (!cancelled) {
          setResult({
            key: filtersKey,
            error: e instanceof ApiError ? e.message : "회원 목록을 불러오지 못했습니다.",
          });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [keyword, page, filtersKey]);

  useEffect(() => {
    let cancelled = false;
    fetchAdminMemberSummary()
      .then((response) => {
        if (!cancelled) setSummary(response);
      })
      .catch(() => {
        // 요약은 보조 정보라 실패해도 목록 조회를 막지 않는다
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const loading = result?.key !== filtersKey;
  const loaded = (!loading && result?.page?.content) || [];
  const error = !loading ? (result?.error ?? null) : null;
  const totalPages = result?.page?.totalPages ?? 0;

  // 티어가 목업 값이라 서버는 티어를 모른다. 그래서 이 필터는 불러온 페이지 안에서만 동작한다.
  const rows = tier === ALL ? loaded : loaded.filter((m) => mockMemberMeta(m.id).tier === tier);

  const emptyText = loading ? "회원 목록을 불러오는 중…" : (error ?? "조건에 맞는 회원이 없습니다.");

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center gap-3">
        <FilterSelect
          label="티어"
          value={tier}
          options={TIER_OPTIONS}
          onChange={setTier}
        />

        <div className="relative min-w-[220px] flex-1">
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
            onChange={(e) => setQuery(e.target.value)}
            placeholder="닉네임 · 이메일 검색"
            className="pl-[46px]"
          />
        </div>

        <button
          type="button"
          onClick={() => {
            setTier(ALL);
            setQuery("");
            setPage(0);
          }}
          className="flex-shrink-0 cursor-pointer whitespace-nowrap px-1 text-[13.5px] font-semibold text-secondary transition-colors hover:text-ink"
        >
          초기화
        </button>
      </div>

      <div className="flex flex-wrap items-center gap-2 px-0.5 text-[13px] font-semibold text-secondary">
        <span>
          총{" "}
          <span className="font-mono font-bold text-ink">
            {(summary?.totalCount ?? 0).toLocaleString()}
          </span>
          명
        </span>
        <span className="text-icon">·</span>
        <span>
          최근 7일 활동{" "}
          <span className="font-mono font-bold text-ink">
            {(summary?.activeWeekCount ?? 0).toLocaleString()}
          </span>
          명
        </span>
        {tier !== ALL && (
          <span className="text-[12px] font-medium text-placeholder">
            (티어 필터는 현재 페이지에만 적용됩니다)
          </span>
        )}
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={rows}
        rowKey={(m) => String(m.id)}
        onRowClick={setSelected}
        emptyText={emptyText}
      />

      {!error && <Pagination page={page} totalPages={totalPages} onChange={setPage} />}

      {selected && <MemberDetailModal member={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
