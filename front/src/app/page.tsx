import Link from "next/link";
import type { LearningMenuIcon, MetricTone } from "@/types";
import TodayDate from "@/components/ui/TodayDate";
import { learningStats } from "@/mocks/user";
import { learningMenu, todayGoal, todayMetrics } from "@/mocks/today";

// 학습 메뉴 카드 아이콘 (더미 UI용 인라인 SVG)
function MenuCardIcon({ name, color }: { name: LearningMenuIcon; color: string }) {
  const common = {
    width: 21,
    height: 21,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: color,
    strokeWidth: 1.8,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };
  switch (name) {
    case "solve":
      return (
        <svg {...common}>
          <path d="M9 11l3 3L22 4" />
          <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
        </svg>
      );
    case "wrong":
      return (
        <svg {...common}>
          <path d="M4 4v16h16" />
          <path d="M8 16l3-4 2 2 4-6" />
        </svg>
      );
    case "interview":
      return (
        <svg {...common}>
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
        </svg>
      );
    case "mock":
      return (
        <svg {...common}>
          <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
        </svg>
      );
  }
}

// 지표 카드 값 색상
const metricValueColor: Record<MetricTone, string> = {
  default: "text-[#1C1C1A]",
  warning: "text-[#C2410C]",
  success: "text-[#16A34A]",
};

export default function Home() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      {/* 페이지 헤더 (제목 + 오늘 날짜) */}
      <div className="border-b border-[#E6E6E0] bg-[#F6F6F4]/85 py-[18px]">
        <div className="mx-auto flex w-full max-w-[1180px] items-end justify-between px-9">
          <div className="flex flex-col gap-[3px]">
            <h1 className="text-[21px] font-bold tracking-[-0.4px] text-[#1C1C1A]">
              오늘의 학습
            </h1>
            <p className="text-[13px] text-[#8A8A80]">
              매일의 학습을 한 화면에서 시작하세요
            </p>
          </div>
          <div className="flex items-center gap-1.5 rounded-[9px] border border-[#ECECE8] bg-white px-3 py-[7px] text-[12.5px] font-medium text-[#6B6B62]">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <path d="M16 2v4M8 2v4M3 10h18" />
            </svg>
            <TodayDate />
          </div>
        </div>
      </div>

      {/* 본문 */}
      <div className="mx-auto w-full max-w-[1180px] flex-1 self-center px-9 pb-[60px] pt-8">
        <div className="flex flex-col gap-[22px]">
          {/* 오늘 상태 배너 */}
          <div className="flex items-center justify-between gap-6 overflow-hidden rounded-[18px] bg-[#1C1C1A] px-7 py-[26px] text-white">
            <div className="flex flex-col gap-2.5">
              <div>
                <span className="inline-flex items-center gap-1.5 rounded-[20px] bg-[#34D36B]/[0.16] px-2.5 py-1 text-xs font-semibold text-[#5DDC8A]">
                  ● {todayGoal.completed ? "오늘 학습 완료" : "오늘 학습 진행 중"}
                </span>
              </div>
              <div className="text-[23px] font-bold leading-[1.35] tracking-[-0.4px]">
                오늘도 꾸준히 이어가고 있어요
              </div>
              <div className="text-[13.5px] text-[#A8A8A0]">
                최소 학습 목표{" "}
                <span className="font-mono font-semibold text-white">
                  {todayGoal.target}문제
                </span>{" "}
                중{" "}
                <span className="font-mono font-semibold text-[#5DDC8A]">
                  {todayGoal.current}문제
                </span>{" "}
                완료
              </div>
              <div className="mt-1 h-[7px] w-[300px] max-w-full overflow-hidden rounded-md bg-white/[0.12]">
                <div
                  className="h-full rounded-md bg-[#5DDC8A]"
                  style={{
                    width: `${Math.min(100, (todayGoal.current / todayGoal.target) * 100)}%`,
                  }}
                />
              </div>
            </div>
            <div className="flex gap-[30px]">
              <div className="flex flex-col items-center gap-[3px]">
                <span className="font-mono text-[34px] font-bold leading-none text-white">
                  {learningStats.streakDays}
                </span>
                <span className="text-xs text-[#A8A8A0]">연속 학습일</span>
              </div>
              <div className="w-px bg-white/[0.12]" />
              <div className="flex flex-col items-center gap-[3px]">
                <span className="font-mono text-[34px] font-bold leading-none text-white">
                  {learningStats.cumulativeDays}
                </span>
                <span className="text-xs text-[#A8A8A0]">누적 학습일</span>
              </div>
            </div>
          </div>

          {/* 오늘 지표 카드 */}
          <div className="grid grid-cols-3 gap-3.5">
            {todayMetrics.map((m) => (
              <div
                key={m.key}
                className="flex flex-col gap-[7px] rounded-[14px] border border-[#ECECE8] bg-white px-5 py-[18px]"
              >
                <span className="text-[12.5px] font-medium text-[#8A8A80]">
                  {m.label}
                </span>
                {m.unit ? (
                  <div className="flex items-baseline gap-1.5">
                    <span className="font-mono text-[28px] font-bold text-[#1C1C1A]">
                      {m.value}
                    </span>
                    <span className="text-[13px] text-[#A8A8A0]">{m.unit}</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-[7px]">
                    <span className={`text-xl font-bold ${metricValueColor[m.tone]}`}>
                      {m.value}
                    </span>
                    {m.note && (
                      <span className="text-[13px] text-[#A8A8A0]">{m.note}</span>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* 오늘 완료 가능한 학습 */}
          <div>
            <div className="mb-[13px] text-[13px] font-semibold text-[#8A8A80]">
              오늘 완료 가능한 학습
            </div>
            <div className="grid grid-cols-2 gap-3.5">
              {learningMenu.map((item) => (
                <Link
                  key={item.key}
                  href={item.href}
                  className="flex items-center gap-4 rounded-[14px] border border-[#ECECE8] bg-white px-[22px] py-5 text-left transition-colors hover:border-[#1C1C1A]"
                >
                  <div
                    className="flex h-[42px] w-[42px] flex-shrink-0 items-center justify-center rounded-[11px]"
                    style={{ backgroundColor: item.accentBg }}
                  >
                    <MenuCardIcon name={item.icon} color={item.accentFg} />
                  </div>
                  <div className="flex-1">
                    <div className="mb-[3px] flex items-center gap-[7px]">
                      <span className="text-[15px] font-semibold text-[#1C1C1A]">
                        {item.title}
                      </span>
                      {item.badge && (
                        <span className="rounded-[5px] bg-[#F0EDFF] px-1.5 py-0.5 text-[10px] font-bold tracking-[0.03em] text-[#6D28D9]">
                          {item.badge}
                        </span>
                      )}
                    </div>
                    <div className="text-[12.5px] text-[#9A9A90]">
                      {item.description}
                    </div>
                  </div>
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="#C8C8C0"
                    strokeWidth="2"
                    strokeLinecap="round"
                  >
                    <path d="M9 18l6-6-6-6" />
                  </svg>
                </Link>
              ))}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
