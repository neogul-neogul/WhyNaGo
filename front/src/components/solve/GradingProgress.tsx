"use client";

import { useEffect, useState } from "react";

/**
 * AI 채점 대기 표시.
 *
 * 채점은 보통 5~7초, 길면 20초를 넘긴다. 그동안 화면에서 움직이는 것이 없으면
 * 사용자가 멈춘 것으로 오해하고 이탈하므로, 문구·인디케이터·스켈레톤을 함께
 * 움직여 진행 중임을 계속 알린다.
 *
 * 단계 문구는 실제 진행률이 아니다 — 백엔드가 채점과 꼬리질문 생성을 한 번의 AI
 * 호출로 처리해서 중간 상태를 알 수 없다. 경과 시간에 따라 "지금쯤 무슨 일이
 * 일어나고 있는지"를 순서대로 안내하는 문구다.
 */
const STAGES = [
  { afterSeconds: 0, text: "답변을 읽고 있어요" },
  { afterSeconds: 3, text: "채점하고 있어요" },
  { afterSeconds: 8, text: "꼬리질문을 준비하고 있어요" },
  { afterSeconds: 16, text: "거의 다 됐어요. 조금만 기다려 주세요" },
];

export default function GradingProgress() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => setSeconds((prev) => prev + 1), 1000);
    return () => clearInterval(timer);
  }, []);

  const stage = STAGES.reduce((acc, s) => (seconds >= s.afterSeconds ? s : acc), STAGES[0]);

  return (
    <div
      className="flex flex-col gap-3 rounded-[12px] border border-ai-line bg-ai-bg px-4 py-3.5"
      aria-live="polite"
      aria-busy="true"
    >
      <div className="flex items-center gap-2">
        <span className="inline-flex h-5 w-5 items-center justify-center rounded-[5px] bg-ai text-[10px] font-bold text-white">
          AI
        </span>
        <span className="text-[13px] font-bold text-ai-deep">{stage.text}</span>
        {/* 타이핑 인디케이터 — 문구가 그대로일 때도 화면이 살아 있게 한다 */}
        <span className="flex items-center gap-[3px]" aria-hidden="true">
          <span className="h-[3px] w-[3px] animate-bounce rounded-full bg-ai" />
          <span className="h-[3px] w-[3px] animate-bounce rounded-full bg-ai [animation-delay:0.15s]" />
          <span className="h-[3px] w-[3px] animate-bounce rounded-full bg-ai [animation-delay:0.3s]" />
        </span>
      </div>
      {/* 피드백이 들어올 자리를 미리 잡아둔다 — 결과가 어디에 나타날지 예고하는 역할 */}
      <div className="flex flex-col gap-[7px]" aria-hidden="true">
        <div className="h-2.5 w-full animate-pulse rounded-full bg-ai-line" />
        <div className="h-2.5 w-[86%] animate-pulse rounded-full bg-ai-line [animation-delay:0.2s]" />
        <div className="h-2.5 w-[64%] animate-pulse rounded-full bg-ai-line [animation-delay:0.4s]" />
      </div>
    </div>
  );
}
