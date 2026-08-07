"use client";

import { useEffect, useRef, useState } from "react";
import { formatRemaining } from "@/lib/interviews";

/** 이 시간 이하로 남으면 경고 색으로 바꾼다 */
const WARNING_THRESHOLD_SECONDS = 30;

/**
 * 문항당 제한 시간 타이머.
 *
 * - 문항이 바뀌면(`questionIndex`) 남은 시간을 처음부터 다시 센다.
 *   본 질문·꼬리질문1·꼬리질문2가 각각 독립된 제한 시간을 갖는다.
 * - `paused` 동안은 멈추고, 풀리면 멈춘 시점의 남은 시간에서 이어간다.
 *   채점 실패로 같은 문항을 재시도할 때 시간을 되돌려주지 않기 위해서다.
 * - 남은 시간은 목표 시각과의 차이로 계산한다 — setInterval 누적 방식은
 *   탭이 비활성일 때 콜백이 지연돼 실제 경과 시간과 어긋난다.
 */
export default function QuestionTimer({
  seconds,
  questionIndex,
  paused = false,
  onExpire,
}: {
  seconds: number;
  /** 현재 문항 순번. 값이 바뀌면 타이머를 리셋한다 */
  questionIndex: number;
  paused?: boolean;
  onExpire: () => void;
}) {
  const [remaining, setRemaining] = useState(seconds);
  const [trackedIndex, setTrackedIndex] = useState(questionIndex);
  // 만료 콜백은 문항당 정확히 1회만 — 자동 제출이 중복되지 않게 막는다
  const expiredRef = useRef(false);
  const onExpireRef = useRef(onExpire);
  useEffect(() => {
    onExpireRef.current = onExpire;
  }, [onExpire]);

  // 문항이 바뀌면 남은 시간을 초기화한다.
  // 렌더 중 상태 조정은 prop 변화에 state를 맞추는 React 공식 패턴이다
  // (이펙트로 하면 이전 시간이 한 프레임 보였다가 바뀐다).
  if (trackedIndex !== questionIndex) {
    setTrackedIndex(questionIndex);
    setRemaining(seconds);
  }

  // 만료 플래그도 문항 단위로 초기화한다 (ref 쓰기는 이펙트 안에서만)
  useEffect(() => {
    expiredRef.current = false;
  }, [questionIndex]);

  useEffect(() => {
    if (paused || expiredRef.current) return;

    // 일시정지 후 재개하면 남은 시간만큼 목표 시각을 다시 잡는다
    const deadline = Date.now() + remaining * 1000;

    const tick = () => {
      const left = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
      setRemaining(left);
      if (left === 0 && !expiredRef.current) {
        expiredRef.current = true;
        onExpireRef.current();
      }
    };

    tick();
    const timer = setInterval(tick, 250);
    return () => clearInterval(timer);
    // remaining을 의존성에 넣으면 매 틱마다 목표 시각이 밀려 타이머가 멈추지 않는다.
    // 목표 시각은 문항 전환·일시정지 전환 시에만 다시 잡는다.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [paused, questionIndex]);

  const warning = remaining <= WARNING_THRESHOLD_SECONDS;

  return (
    <span
      role="timer"
      aria-label={`남은 시간 ${formatRemaining(remaining)}`}
      className={`inline-flex items-center gap-1.5 rounded-[8px] px-2.5 py-1 font-mono text-[13px] font-bold tabular-nums ${
        warning ? "bg-danger-bg text-danger" : "bg-neutral text-secondary"
      }`}
    >
      <svg
        width="14"
        height="14"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="9" />
        <path d="M12 7v5l3 2" />
      </svg>
      {formatRemaining(remaining)}
      {paused && <span className="text-[11px] font-semibold">일시정지</span>}
    </span>
  );
}
