import type { MasteryLevel } from "@/types";
import {
  MASTERY_LEVEL_COLORS,
  MASTERY_LEVEL_DESCRIPTIONS,
  MASTERY_LEVEL_LABELS,
} from "@/lib/mastery";

/**
 * 채점 시점에 보여주는 이해 수준 판정과 그 근거.
 *
 * 점수·통과 여부와 따로 두는 이유는 둘이 다른 것을 말하기 때문이다. 점수는 루브릭 항목을 몇 개
 * 충족했는지이고, 판정은 그 답변이 드러낸 **이해 상태**다. 통과했지만 근거가 흔들리는 답변,
 * 미통과지만 개념은 잡힌 답변이 모두 있다.
 *
 * AI가 판정을 빠뜨리면 null이 온다. 그때는 빈 칸을 두지 않고 아무것도 그리지 않는다 —
 * 채점은 정상이므로 오류로 보이게 하면 안 된다.
 */
export default function MasteryVerdict({
  mastery,
  masteryReason,
}: {
  mastery: MasteryLevel | null;
  masteryReason: string | null;
}) {
  if (!mastery) return null;

  return (
    <div className="mt-3 border-t border-line-soft pt-3">
      <div className="flex flex-wrap items-baseline gap-2">
        <span className="text-[11.5px] font-bold text-secondary">이해 수준</span>
        <span
          className="rounded-[5px] px-1.5 py-[1px] text-[10.5px] font-bold text-white"
          style={{ background: MASTERY_LEVEL_COLORS[mastery] }}
        >
          {MASTERY_LEVEL_LABELS[mastery]}
        </span>
        <span className="text-[11.5px] text-soft">{MASTERY_LEVEL_DESCRIPTIONS[mastery]}</span>
      </div>
      {masteryReason && (
        <div className="mt-1.5 text-[13px] leading-[1.65] text-secondary">{masteryReason}</div>
      )}
    </div>
  );
}
