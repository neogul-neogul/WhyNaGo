"use client";

import { useSyncExternalStore } from "react";

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"] as const;

function formatToday(date: Date): string {
  const y = date.getFullYear();
  const m = date.getMonth() + 1;
  const d = date.getDate();
  return `${y}년 ${m}월 ${d}일 (${WEEKDAYS[date.getDay()]})`;
}

const subscribe = () => () => {};

/**
 * 오늘 날짜를 사용자의 로컬 기준 현재 날짜로 표시한다.
 * 서버에서는 빈 값, 클라이언트에서는 실제 현재 날짜를 반환해
 * hydration 불일치 없이 로컬 시각을 렌더한다.
 */
export default function TodayDate() {
  const label = useSyncExternalStore(
    subscribe,
    () => formatToday(new Date()),
    () => "",
  );

  return <span>{label}</span>;
}
