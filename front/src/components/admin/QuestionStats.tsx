"use client";

import { useState } from "react";
import type { AdminQuestionSession } from "@/types";
import type { AdminQuestionView } from "@/mocks/admin";
import Card, { CardHeader } from "@/components/ui/Card";
import Pagination from "@/components/ui/Pagination";
import StatCard from "@/components/ui/StatCard";
import AdminTable, { type AdminColumn } from "@/components/admin/AdminTable";
import {
  CategoryBadge,
  DifficultyBadge,
  ResultBadge,
  TypeBadge,
} from "@/components/admin/AdminBadges";

// 시안 기준 — 최신 500건까지 조회하는 세션 목록의 총 페이지 수
const SESSION_TOTAL_PAGES = 264;

const SESSION_COLUMNS: AdminColumn<AdminQuestionSession>[] = [
  {
    key: "user",
    header: "사용자",
    width: 130,
    render: (s) => <span className="text-sm font-semibold text-ink">{s.user}</span>,
  },
  {
    key: "pick",
    header: "선택한 보기",
    render: (s) => <span className="block truncate text-sm font-medium text-ink">{s.pick}</span>,
  },
  {
    key: "result",
    header: "정오답",
    width: 64,
    align: "right",
    render: (s) => <ResultBadge result={s.correct ? "정답" : "오답"} />,
  },
  {
    key: "spent",
    header: "소요 시간",
    width: 88,
    align: "right",
    render: (s) => (
      <span className="font-mono text-[12.5px] font-medium text-secondary">{s.spent}</span>
    ),
  },
  {
    key: "at",
    header: "풀이 일시",
    width: 110,
    align: "right",
    render: (s) => (
      <span className="font-mono text-[12.5px] font-medium text-placeholder">{s.at}</span>
    ),
  },
];

// 문제 상세 · 통계 탭
export default function QuestionStats({ question }: { question: AdminQuestionView }) {
  const [sessionPage, setSessionPage] = useState(0);
  const { detail } = question;

  return (
    <div className="flex flex-col gap-[18px]">
      <Card className="flex items-center gap-2.5 px-[22px] py-[18px]">
        <span className="font-mono text-[13.5px] font-semibold text-secondary">{question.id}</span>
        <CategoryBadge category={question.category} />
        <DifficultyBadge difficulty={question.difficulty} />
        <TypeBadge type={question.type} />
        <span className="ml-1.5 text-[15px] font-bold text-ink">{detail.title}</span>
      </Card>

      <div className="grid grid-cols-4 gap-5">
        {question.statCards.map((card) => (
          <StatCard key={card.label} label={card.label}>
            <span className="font-mono text-[26px] font-bold tracking-[-0.5px] text-ink">
              {card.value}
              {card.unit && (
                <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">
                  {card.unit}
                </span>
              )}
            </span>
          </StatCard>
        ))}
      </div>

      {detail.distribution && (
        <Card className="overflow-hidden">
          <CardHeader className="justify-between gap-3">
            <span className="text-[13px] font-semibold text-secondary">보기별 선택 분포</span>
            <span className="flex-shrink-0 whitespace-nowrap text-[12.5px] font-medium text-placeholder">
              응답{" "}
              <span className="font-mono font-semibold text-secondary">
                {(detail.responseCount ?? 0).toLocaleString()}
              </span>
              건
            </span>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-[18px]">
            {detail.distribution.map((row) => (
              <div key={row.label} className="flex items-center gap-4">
                <span
                  className={`w-[60px] flex-shrink-0 text-[12.5px] font-semibold ${
                    row.correct ? "text-success" : "text-placeholder"
                  }`}
                >
                  {row.label}
                </span>
                <span
                  className={`w-[300px] flex-shrink-0 truncate text-sm text-ink ${
                    row.correct ? "font-bold" : "font-medium"
                  }`}
                >
                  {row.text}
                </span>
                <div className="h-2 min-w-0 flex-1 overflow-hidden rounded-md bg-neutral">
                  {/* 폭이 데이터에서 오므로 유틸리티 클래스로 표현할 수 없다 */}
                  <div
                    style={{ width: row.rate }}
                    className={`h-full rounded-md ${row.correct ? "bg-success" : "bg-placeholder"}`}
                  />
                </div>
                <span className="w-14 flex-shrink-0 text-right font-mono text-[13.5px] font-medium text-secondary">
                  {row.count.toLocaleString()}
                </span>
                <span
                  className={`w-14 flex-shrink-0 text-right font-mono text-[13.5px] font-bold ${
                    row.correct ? "text-success" : "text-secondary"
                  }`}
                >
                  {row.rate}
                </span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {detail.sessions && (
        <AdminTable
          columns={SESSION_COLUMNS}
          rows={detail.sessions}
          rowKey={(s) => `${s.user}-${s.at}`}
          emptyText="아직 풀이 세션이 없습니다."
          caption={{
            left: "최근 풀이 세션",
            right: (
              <span className="flex-shrink-0 whitespace-nowrap text-[12.5px] font-medium text-placeholder">
                최신 500건까지 조회
              </span>
            ),
          }}
          footer={
            <div className="px-[22px] py-4">
              <Pagination
                page={sessionPage}
                totalPages={SESSION_TOTAL_PAGES}
                onChange={setSessionPage}
              />
            </div>
          }
        />
      )}
    </div>
  );
}
