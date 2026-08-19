"use client";

import type { CSSProperties, ReactNode } from "react";
import Card, { CardHeader } from "@/components/ui/Card";

export interface AdminColumn<T> {
  key: string;
  header: ReactNode;
  /** 고정 폭(px). 지정하지 않으면 남는 공간을 차지한다 */
  width?: number;
  align?: "right";
  render: (row: T) => ReactNode;
}

// 컬럼 폭은 값이 컬럼 정의에서 오므로 유틸리티 클래스로 표현할 수 없다
function cellStyle(column: AdminColumn<unknown>): CSSProperties {
  return column.width === undefined
    ? { flex: 1, minWidth: 0 }
    : { width: column.width, flexShrink: 0 };
}

const ROW_GAP = 16; // gap-4
const ROW_PADDING = 44; // px-[22px] 양쪽
const FLEX_COLUMN_MIN = 180; // 폭을 지정하지 않은 컬럼이 확보해야 할 최소 폭

/**
 * 창을 좁혀도 컬럼이 잘리지 않도록, 행이 필요로 하는 최소 폭을 계산한다.
 * 이 폭 아래로는 카드가 가로 스크롤된다.
 */
function trackMinWidth(columns: AdminColumn<unknown>[]): number {
  const columnsWidth = columns.reduce((sum, c) => sum + (c.width ?? FLEX_COLUMN_MIN), 0);
  return ROW_PADDING + ROW_GAP * Math.max(0, columns.length - 1) + columnsWidth;
}

/**
 * 관리자 화면 공통 테이블.
 * 컬럼 정의(고정 폭 + 정렬 + 셀 렌더러)만 넘기면 헤더·행·빈 상태를 동일한 규격으로 그린다.
 * caption을 주면 카드 상단에 헤더 스트립이 붙고, 컬럼 헤더는 스트립과 겹치지 않게 흰 배경이 된다.
 */
export default function AdminTable<T>({
  columns,
  rows,
  rowKey,
  onRowClick,
  rowClassName,
  emptyText,
  caption,
  footer,
}: {
  columns: AdminColumn<T>[];
  rows: T[];
  rowKey: (row: T) => string;
  onRowClick?: (row: T) => void;
  rowClassName?: (row: T) => string;
  emptyText: string;
  caption?: { left: ReactNode; right?: ReactNode };
  footer?: ReactNode;
}) {
  const headers = (
    <div
      className={`flex items-center gap-4 border-b border-line-card px-[22px] py-[13px] text-xs font-semibold text-placeholder ${
        caption ? "bg-white" : "bg-subtle"
      }`}
    >
      {columns.map((c) => (
        <span
          key={c.key}
          style={cellStyle(c as AdminColumn<unknown>)}
          className={c.align === "right" ? "text-right" : undefined}
        >
          {c.header}
        </span>
      ))}
    </div>
  );

  return (
    <Card className="overflow-hidden">
      {caption && (
        <CardHeader className="flex-wrap justify-between gap-3">
          <span className="text-[13px] font-semibold text-secondary">{caption.left}</span>
          {caption.right}
        </CardHeader>
      )}

      {/* 컬럼 폭 합계보다 좁아지면 헤더와 행이 함께 가로 스크롤된다 */}
      <div className="overflow-x-auto">
        <div style={{ minWidth: trackMinWidth(columns as AdminColumn<unknown>[]) }}>
          {headers}

          {rows.map((row) => {
            const cells = columns.map((c) => (
              <span
                key={c.key}
                style={cellStyle(c as AdminColumn<unknown>)}
                className={c.align === "right" ? "text-right" : undefined}
              >
                {c.render(row)}
              </span>
            ));
            const className = `flex w-full items-center gap-4 border-b border-line-soft px-[22px] py-[15px] text-left ${
              rowClassName?.(row) ?? "bg-white"
            }`;

            return onRowClick ? (
              <button
                key={rowKey(row)}
                type="button"
                onClick={() => onRowClick(row)}
                className={`${className} cursor-pointer transition-colors hover:bg-subtle`}
              >
                {cells}
              </button>
            ) : (
              <div key={rowKey(row)} className={className}>
                {cells}
              </div>
            );
          })}

          {rows.length === 0 && (
            <div className="px-[22px] py-11 text-center text-[13.5px] font-medium text-placeholder">
              {emptyText}
            </div>
          )}
        </div>
      </div>

      {footer}
    </Card>
  );
}
