/**
 * 스트릭(연속·누적 학습일)의 전역 캐시와 구독을 전담한다.
 * authStorage.ts와 같은 패턴(모듈 캐시 + 커스텀 이벤트 + useSyncExternalStore)을 따른다.
 * 로그인 직후·문제 풀이 세션 저장 직후처럼 서버 값이 바뀔 수 있는 지점에서
 * refreshStreak()를 불러 모든 구독자(헤더 배지·홈 배너)를 함께 갱신한다.
 */

import { useSyncExternalStore } from "react";
import { fetchStreak } from "@/lib/records";
import type { StreakResponse } from "@/types";

const STREAK_EVENT = "whynago:streak-change";

let cached: StreakResponse | null = null;
let inflight: Promise<void> | null = null;
// resetStreak()이 증가시켜, 세션이 바뀐 뒤 늦게 도착한 이전 세션의 응답을 폐기한다
let generation = 0;

function notify() {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new Event(STREAK_EVENT));
}

function subscribeStreak(callback: () => void) {
  if (typeof window === "undefined") return () => {};
  window.addEventListener(STREAK_EVENT, callback);
  return () => window.removeEventListener(STREAK_EVENT, callback);
}

function getStreakSnapshot(): StreakResponse | null {
  return cached;
}

/**
 * 서버에서 스트릭을 다시 조회해 캐시를 갱신하고 구독자에게 통지한다.
 * 동시에 여러 곳에서 불러도 요청은 한 번만 나간다.
 * 조회에 실패하면 직전 값을 그대로 둔다(0으로 덮어쓰지 않는다).
 */
export function refreshStreak(): Promise<void> {
  if (inflight) return inflight;

  const gen = generation;
  inflight = fetchStreak()
    .then((result) => {
      if (gen !== generation) return;
      cached = result;
      notify();
    })
    .catch(() => {
      // 헤더·홈 배너는 조회에 실패해도 화면을 막지 않는다
    })
    .finally(() => {
      inflight = null;
    });
  return inflight;
}

/** 로그아웃 시 이전 사용자의 스트릭 잔상을 지운다 */
export function resetStreak() {
  generation += 1;
  inflight = null;
  cached = null;
  notify();
}

/** 스트릭을 반응형으로 구독하는 훅 (아직 조회 전이면 null) */
export function useStreak(): StreakResponse | null {
  return useSyncExternalStore(subscribeStreak, getStreakSnapshot, () => null);
}