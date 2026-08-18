"use client";

import { useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { AdminSolveRecord } from "@/types";
import {
  ADMIN_MEMBER_TABS,
  type AdminMemberTab,
  adminMembers,
  adminSolveRecords,
  adminSolveTotal,
} from "@/mocks/admin";
import Card from "@/components/ui/Card";
import Chip from "@/components/ui/Chip";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import {
  CategoryBadge,
  DifficultyText,
  ResultBadge,
  TierBadge,
} from "@/components/admin/AdminBadges";

const PAGE_STEP = 5;

const COLUMNS: AdminColumn<AdminSolveRecord>[] = [
  {
    key: "at",
    header: "일시",
    width: 106,
    render: (r) => <span className="font-mono text-[12.5px] font-medium text-secondary">{r.at}</span>,
  },
  {
    key: "questionId",
    header: "ID",
    width: 58,
    render: (r) => (
      <span className="font-mono text-[12.5px] font-medium text-secondary">{r.questionId}</span>
    ),
  },
  { key: "category", header: "카테고리", width: 132, render: (r) => <CategoryBadge category={r.category} /> },
  {
    key: "difficulty",
    header: "난이도",
    width: 74,
    render: (r) => <DifficultyText difficulty={r.difficulty} />,
  },
  {
    key: "title",
    header: "문제 본문",
    render: (r) => <span className="block truncate text-sm font-semibold text-ink">{r.title}</span>,
  },
  {
    key: "type",
    header: "유형",
    width: 56,
    render: (r) => <span className="text-[13px] font-medium text-secondary">{r.type}</span>,
  },
  {
    key: "score",
    header: "점수",
    width: 44,
    align: "right",
    render: (r) => <span className="font-mono text-[13.5px] font-semibold text-ink">{r.score}</span>,
  },
  {
    key: "spent",
    header: "소요",
    width: 78,
    align: "right",
    render: (r) => (
      <span className="font-mono text-[12.5px] font-medium text-secondary">{r.spent}</span>
    ),
  },
  {
    key: "result",
    header: "결과",
    width: 56,
    align: "right",
    render: (r) => <ResultBadge result={r.result} />,
  },
];

export default function AdminMemberDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [tab, setTab] = useState<AdminMemberTab>("풀이 이력");
  const [shown, setShown] = useState(PAGE_STEP);

  const member = adminMembers.find((m) => m.id === id);
  if (!member) {
    return (
      <div className="px-[22px] py-10 text-center text-[13.5px] text-soft">
        회원을 찾을 수 없습니다.
      </div>
    );
  }

  const records = adminSolveRecords(member, tab);
  const total = adminSolveTotal(member, tab);
  const rows = records.slice(0, shown);

  const stats = [
    { label: "누적 점수", value: member.score.toLocaleString(), unit: "" },
    { label: "연속 학습일", value: String(member.streakDays), unit: "일" },
    { label: "누적 학습일", value: String(member.cumulativeDays), unit: "일" },
    { label: "가입일", value: member.joinedAt, unit: "" },
    { label: "최근 접속", value: member.lastVisitedAt, unit: "" },
  ];

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <Link
        href="/admin/members"
        className="self-start text-[13px] font-semibold text-secondary transition-colors hover:text-ink"
      >
        ← 회원 목록
      </Link>

      <Card className="flex flex-col gap-5 p-6">
        <div className="flex items-center gap-[18px]">
          <div className="flex h-[62px] w-[62px] flex-shrink-0 items-center justify-center rounded-full border border-line-card bg-neutral text-xl font-bold text-secondary">
            {member.nickname.charAt(0).toUpperCase()}
          </div>
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-[11px]">
              <span className="text-xl font-bold tracking-[-0.4px] text-ink">{member.nickname}</span>
              <TierBadge tier={member.tier} />
            </div>
            <span className="text-[13.5px] font-medium text-secondary">
              {member.email} · {member.position} · {member.id}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-5 gap-3 border-t border-dashed border-line pt-5">
          {stats.map((s) => (
            <div key={s.label} className="flex flex-col gap-[7px]">
              <span className="text-[12.5px] font-semibold text-placeholder">{s.label}</span>
              <span className="font-mono text-[22px] font-bold tracking-[-0.5px] text-ink">
                {s.value}
                {s.unit && <span className="font-sans text-sm text-placeholder">{s.unit}</span>}
              </span>
            </div>
          ))}
        </div>
      </Card>

      <div className="flex items-center gap-2.5">
        {ADMIN_MEMBER_TABS.map((t) => (
          <Chip
            key={t}
            label={t}
            active={tab === t}
            onClick={() => {
              setTab(t);
              setShown(PAGE_STEP);
            }}
          />
        ))}
      </div>

      <AdminTable
        columns={COLUMNS}
        rows={rows}
        rowKey={(r) => `${r.at}-${r.questionId}`}
        emptyText="해당 조건의 이력이 없습니다."
        caption={{
          left: tab,
          right: (
            <span className="flex-shrink-0 whitespace-nowrap text-[12.5px] font-medium text-placeholder">
              총 <span className="font-mono font-semibold text-secondary">{total.toLocaleString()}</span>건
            </span>
          ),
        }}
        footer={
          <div className="flex items-center justify-between gap-3 px-[22px] py-3.5">
            <span className="font-mono text-[12.5px] font-medium text-placeholder">
              1-{Math.min(shown, records.length)} / {total.toLocaleString()}
            </span>
            {shown < records.length && (
              <button
                type="button"
                onClick={() => setShown((v) => Math.min(v + PAGE_STEP, records.length))}
                className="cursor-pointer text-[13px] font-semibold text-secondary transition-colors hover:text-ink"
              >
                더 보기
              </button>
            )}
          </div>
        }
      />
    </div>
  );
}
