"use client";

/**
 * 면접 중 화면 이탈 감지 훅.
 *
 * 한계: 브라우저는 "다른 모니터의 다른 프로그램을 보는 것"까지 감지할 수 없다.
 * 창이 포커스를 잃거나 탭이 가려지는 경우만 잡힌다.
 */

import { useEffect, useRef } from "react";
import { FOCUS_LOSS_DEBOUNCE_MS } from "@/lib/interviews";

/**
 * 이탈 1회를 정확히 1번만 세어 onLeave로 알린다.
 *
 * - 감지 신호: `visibilitychange`(→ hidden) = 탭 전환·최소화, `window.blur` = 다른 창·프로그램 전환.
 *   두 신호는 함께 오는 경우가 많아 "이탈 중" 플래그로 잠가 중복 카운트를 막는다.
 * - 복귀(탭이 보이고 포커스도 있음) 시점에 카운트한다. 짧게 벗어났다 바로 돌아오면
 *   (개발자도구 열기·브라우저 UI 클릭 등) 오탐으로 보고 세지 않는다.
 * - `active`가 false면 감지하지 않는다 (채점 중·면접 종료 후).
 */
export function useFocusGuard({ active, onLeave }: { active: boolean; onLeave: () => void }) {
  const awaySinceRef = useRef<number | null>(null);
  // 콜백이 매 렌더 새로 만들어져도 리스너를 다시 붙이지 않도록 ref로 최신값만 따라간다
  const onLeaveRef = useRef(onLeave);
  useEffect(() => {
    onLeaveRef.current = onLeave;
  }, [onLeave]);

  useEffect(() => {
    if (!active) {
      awaySinceRef.current = null;
      return;
    }

    const markAway = () => {
      if (awaySinceRef.current !== null) return;
      awaySinceRef.current = Date.now();
    };

    const markReturn = () => {
      const since = awaySinceRef.current;
      if (since === null) return;
      // 탭은 보이지만 포커스는 없는 중간 상태에서는 복귀로 치지 않는다
      if (document.visibilityState !== "visible" || !document.hasFocus()) return;
      awaySinceRef.current = null;
      if (Date.now() - since < FOCUS_LOSS_DEBOUNCE_MS) return;
      onLeaveRef.current();
    };

    const handleVisibility = () => {
      if (document.visibilityState === "hidden") markAway();
      else markReturn();
    };

    window.addEventListener("blur", markAway);
    window.addEventListener("focus", markReturn);
    document.addEventListener("visibilitychange", handleVisibility);
    return () => {
      window.removeEventListener("blur", markAway);
      window.removeEventListener("focus", markReturn);
      document.removeEventListener("visibilitychange", handleVisibility);
    };
  }, [active]);
}

/**
 * 새로고침·창 닫기 시 브라우저 기본 확인 창을 띄운다.
 * 면접은 진행 상태를 서버에 저장하지 않아 이탈하면 오늘 자리를 잃는다.
 */
export function useBeforeUnloadWarning(active: boolean) {
  useEffect(() => {
    if (!active) return;
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      // 일부 브라우저는 returnValue가 설정돼야 확인 창을 띄운다
      event.returnValue = "";
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [active]);
}
