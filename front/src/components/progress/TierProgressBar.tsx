import type { ProgressResponse } from "@/types";
import { palette } from "@/lib/tokens";
import { TIER_COLORS, TIER_LABELS } from "@/lib/progress";

const TRACK_REST = "rgba(0,0,0,.08)";
const TRACK_FUTURE = "rgba(0,0,0,.06)";

function segmentRadius(index: number, lastIndex: number) {
  if (index === 0) return "7px 3px 3px 7px";
  if (index === lastIndex) return "3px 7px 7px 3px";
  return "3px";
}

/**
 * 누적 점수의 티어 진행 바.
 * 구간 경계값은 서버가 내려준 tiers를 그대로 쓴다(프런트에 임계값을 복제하지 않는다).
 */
export default function TierProgressBar({ progress }: { progress: ProgressResponse }) {
  const { score, tier, nextTier, scoreToNextTier, tiers, maxScore } = progress;
  const currentIndex = tiers.findIndex((t) => t.tier === tier);
  const lastIndex = tiers.length - 1;
  const overallRatio = maxScore > 0 ? score / maxScore : 0;

  const tooltipAlign =
    overallRatio > 0.85
      ? { transform: "translateX(-100%)", items: "items-end", arrow: "0 4px 0 0" }
      : overallRatio < 0.15
        ? { transform: "translateX(0)", items: "items-start", arrow: "0 0 0 4px" }
        : { transform: "translateX(-50%)", items: "items-center", arrow: "0" };

  const segments = tiers.map((range, i) => {
    const top = i < lastIndex ? tiers[i + 1].minScore : maxScore;
    const span = Math.max(top - range.minScore, 1);
    const isDone = i < currentIndex;
    const isCurrent = i === currentIndex;
    const filledPercent = isCurrent
      ? Math.min(Math.max(((score - range.minScore) / span) * 100, 0), 100)
      : 0;
    const color = TIER_COLORS[range.tier].line;

    return {
      ...range,
      span,
      isDone,
      isCurrent,
      filledPercent,
      color,
      background: isDone
        ? color
        : isCurrent
          ? `linear-gradient(to right, ${color} ${filledPercent.toFixed(1)}%, ${TRACK_REST} 0)`
          : TRACK_FUTURE,
    };
  });

  return (
    <div>
      <div className="mb-2 flex items-baseline justify-between gap-3">
        <span className="text-[12.5px] font-bold text-ink">통합 티어 진행</span>
        <span className="font-mono text-[11.5px] text-secondary">
          {nextTier ? `${TIER_LABELS[nextTier]}까지 ${scoreToNextTier}점` : "최고 티어 도달"}
        </span>
      </div>

      <div className="group relative pt-[34px]">
        <div className="flex h-[14px] gap-[2px]">
          {segments.map((segment, i) => (
            <div
              key={segment.tier}
              className="relative"
              style={{
                flexGrow: segment.span,
                flexBasis: 0,
                background: segment.background,
                borderRadius: segmentRadius(i, lastIndex),
              }}
            >
              {segment.isCurrent && (
                <>
                  <span
                    className="absolute bottom-0 top-0 w-[2px] -translate-x-[1px] rounded-[1px] bg-ink"
                    style={{ left: `${segment.filledPercent.toFixed(1)}%` }}
                  />
                  <span
                    className={`pointer-events-none absolute -top-[34px] flex flex-col whitespace-nowrap opacity-0 transition-opacity duration-150 group-hover:opacity-100 ${tooltipAlign.items}`}
                    style={{
                      left: `${segment.filledPercent.toFixed(1)}%`,
                      transform: tooltipAlign.transform,
                    }}
                  >
                    <span
                      className="flex items-baseline gap-1.5 rounded-[6px] px-2.5 py-1 text-[11px] font-bold text-white shadow-sm"
                      style={{ background: segment.color }}
                    >
                      현재 내 티어 · {TIER_LABELS[segment.tier]}
                      <span className="font-mono font-medium opacity-85">{score}점</span>
                    </span>
                    <span
                      className="h-0 w-0 border-x-[5px] border-t-[5px] border-x-transparent"
                      style={{ borderTopColor: segment.color, margin: tooltipAlign.arrow }}
                    />
                  </span>
                </>
              )}
            </div>
          ))}
        </div>

        <div className="mt-1.5 flex gap-[2px]">
          {segments.map((segment, i) => (
            <span
              key={segment.tier}
              className={`min-w-[48px] whitespace-nowrap font-mono text-[10.5px] ${
                segment.isDone || segment.isCurrent ? "font-bold" : ""
              }`}
              style={{
                flexGrow: segment.span,
                flexBasis: 0,
                textAlign: i === lastIndex ? "right" : "left",
                color: segment.isDone || segment.isCurrent ? segment.color : palette.secondary,
              }}
            >
              {TIER_LABELS[segment.tier]}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
