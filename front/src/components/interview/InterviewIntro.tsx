"use client";

import Link from "next/link";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

type RuleTone = "accent" | "warning" | "alert" | "ai";

const TONE_CLASS: Record<RuleTone, string> = {
  accent: "bg-accent-bg text-accent",
  warning: "bg-warning-bg text-warning",
  alert: "bg-alert-tint text-alert",
  ai: "bg-ai-bg text-ai",
};

const RULES: {
  tone: RuleTone;
  title: string;
  body: string;
  /** stroke 기반 24x24 패스 (currentColor 상속) */
  icon: string;
}[] = [
  {
    tone: "accent",
    title: "총 3문항",
    body: "본 질문 1개와 AI가 만드는 꼬리질문 2개에 이어서 답합니다.",
    icon: "M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01",
  },
  {
    tone: "warning",
    title: "문항당 3분",
    body: "제한 시간이 끝나면 작성 중이던 답변이 그대로 제출됩니다.",
    icon: "M12 21a9 9 0 100-18 9 9 0 000 18zM12 7v5l3 2",
  },
  {
    tone: "alert",
    title: "화면 이탈 감지",
    body: "다른 탭·창으로 이동하면 횟수가 기록되어 결과에 함께 표시됩니다.",
    icon: "M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7zM12 15a3 3 0 100-6 3 3 0 000 6z",
  },
  {
    tone: "ai",
    title: "하루 1회",
    body: "시작하면 오늘 자리가 소진되고, 중간에 나가면 다시 볼 수 없습니다.",
    icon: "M3 10h18M7 3v4M17 3v4M5 5h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2zM12 14h.01",
  },
];

// 면접 시작 전 안내.
// 카테고리 선택 UI는 두지 않는다 — 오늘의 질문은 서버가 정하며 모두에게 같다.
export default function InterviewIntro({
  onStart,
  starting,
  error,
}: {
  onStart: () => void;
  starting: boolean;
  error: string | null;
}) {
  return (
    <Card className="flex flex-col gap-[22px] p-7">
      {/* 오늘의 면접 안내 — 텍스트만 있으면 허전해 동심원 모티프를 배경으로 깐다 */}
      <div className="relative overflow-hidden rounded-[14px] bg-subtle px-6 py-[22px]">
        <div
          className="pointer-events-none absolute -right-10 -top-14 h-56 w-56"
          aria-hidden="true"
        >
          <svg viewBox="0 0 200 200" className="h-full w-full text-accent-line">
            <circle cx="100" cy="100" r="96" fill="none" stroke="currentColor" strokeWidth="1.5" />
            <circle cx="100" cy="100" r="70" fill="none" stroke="currentColor" strokeWidth="1.5" />
            <circle cx="100" cy="100" r="44" fill="none" stroke="currentColor" strokeWidth="1.5" />
            <circle cx="100" cy="100" r="18" className="fill-accent-faint" />
          </svg>
        </div>
        <div className="relative flex max-w-[560px] flex-col gap-2">
          <span className="inline-flex w-fit items-center gap-1.5 rounded-[6px] bg-ai-bg px-2.5 py-[3px] text-xs font-bold text-ai">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M12 3l2.2 5.6L20 10l-5.8 1.4L12 17l-2.2-5.6L4 10l5.8-1.4z" />
            </svg>
            오늘의 면접
          </span>
          <span className="text-[15px] font-bold leading-[1.6] text-ink">
            오늘의 면접은 모든 사용자에게 같습니다
          </span>
          <span className="text-[13.5px] leading-[1.7] text-soft">
            카테고리는 고를 수 없고, 면접을 시작할 때 공개됩니다.
          </span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2.5">
        {RULES.map((rule) => (
          <div
            key={rule.title}
            className="flex items-start gap-3 rounded-[12px] border border-line-card px-4 py-3.5"
          >
            <span
              className={`flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-[10px] ${TONE_CLASS[rule.tone]}`}
              aria-hidden="true"
            >
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d={rule.icon} />
              </svg>
            </span>
            <div className="flex min-w-0 flex-col gap-1">
              <span className="text-[13px] font-bold text-ink">{rule.title}</span>
              <span className="text-[12.5px] leading-[1.6] text-soft">{rule.body}</span>
            </div>
          </div>
        ))}
      </div>

      {error && (
        <div className="rounded-[12px] border border-alert-line bg-alert-bg px-4 py-3.5 text-[13.5px] font-semibold text-alert-deep">
          {error}
        </div>
      )}

      <div className="flex items-center gap-2">
        <Button
          variant="ai"
          size="xl"
          onClick={onStart}
          disabled={starting}
          className="flex items-center gap-2 disabled:opacity-60"
        >
          {starting ? "면접을 준비하는 중…" : "면접 진행"}
          {!starting && (
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
              <path d="M5 12h14M13 6l6 6-6 6" />
            </svg>
          )}
        </Button>
        <Link
          href="/interview/history"
          className="flex items-center gap-1.5 rounded-[11px] border border-line-strong bg-white px-7 py-[13px] text-[15px] font-semibold text-ink transition-colors hover:border-ink"
        >
          면접 기록 보기
        </Link>
      </div>
    </Card>
  );
}
