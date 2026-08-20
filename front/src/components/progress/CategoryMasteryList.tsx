import type { CategoryMasteryResponse, MasteryLevel } from "@/types";
import { categoryLabel } from "@/lib/progress";
import {
  MASTERY_LEVEL_COLORS,
  MASTERY_LEVEL_DESCRIPTIONS,
  MASTERY_LEVEL_LABELS,
  MASTERY_LEVEL_ORDER,
} from "@/lib/mastery";

function total(category: CategoryMasteryResponse) {
  return MASTERY_LEVEL_ORDER.reduce((sum, level) => sum + (category.levelCounts[level] ?? 0), 0);
}

/** 판정 분포를 "아는 것 → 모르는 것" 순서로 쌓은 막대 */
function LevelBar({ category }: { category: CategoryMasteryResponse }) {
  const sum = total(category);
  if (sum <= 0) return null;

  return (
    <span className="flex h-2.5 gap-[2px] overflow-hidden rounded-[5px] bg-neutral">
      {MASTERY_LEVEL_ORDER.map((level) => {
        const count = category.levelCounts[level] ?? 0;
        if (count <= 0) return null;
        return (
          <span
            key={level}
            className="rounded-[4px]"
            style={{
              width: `${((count / sum) * 100).toFixed(1)}%`,
              background: MASTERY_LEVEL_COLORS[level],
            }}
            title={`${MASTERY_LEVEL_LABELS[level]} ${count}문항 — ${MASTERY_LEVEL_DESCRIPTIONS[level]}`}
          />
        );
      })}
    </span>
  );
}

function TagRow({ tag }: { tag: CategoryMasteryResponse["tags"][number] }) {
  return (
    <li className="flex flex-col gap-1 border-b border-line-soft py-2 last:border-b-0">
      <span className="flex flex-wrap items-baseline gap-2">
        <span className="text-[12.5px] font-medium text-ink">{tag.name}</span>
        <span
          className="rounded-[5px] px-1.5 py-[1px] text-[10.5px] font-bold text-white"
          style={{ background: MASTERY_LEVEL_COLORS[tag.level] }}
        >
          {MASTERY_LEVEL_LABELS[tag.level]}
        </span>
      </span>
      {/* AI가 판정 근거를 남기지 못한 이력도 있다. 빈 칸으로 두지 않고 그 사실을 밝힌다. */}
      <span className="text-[11.5px] leading-[1.55] text-secondary">
        {tag.reason ?? "판정 근거가 기록되지 않았습니다."}
      </span>
    </li>
  );
}

/**
 * 카테고리별 숙련도 — 판정 분포와 태그별 현재 숙련도·근거를 함께 보여준다.
 *
 * 근거 문장이 이 화면의 핵심이다. 서술형은 채점 AI가 답변 내용을 보고 판정하면서
 * "무엇을 근거로 그렇게 봤는지"를 함께 쓰는데, 그 문장을 보여주지 않으면
 * 사용자는 라벨만 받고 왜 그런지 알 수 없다.
 */
export default function CategoryMasteryList({
  categories,
}: {
  categories: CategoryMasteryResponse[];
}) {
  const rows = [...categories].sort((a, b) => total(b) - total(a));

  return (
    <div>
      <div className="mb-2 flex flex-wrap items-baseline justify-between gap-2">
        <span className="text-[12.5px] font-bold text-ink">카테고리별 숙련도</span>
        <span className="flex flex-wrap items-center gap-2.5 text-[11px] text-secondary">
          {MASTERY_LEVEL_ORDER.map((level: MasteryLevel) => (
            <span key={level} className="flex items-center gap-1.5">
              <span
                className="inline-block h-2 w-2 rounded-[2px]"
                style={{ background: MASTERY_LEVEL_COLORS[level] }}
              />
              {MASTERY_LEVEL_LABELS[level]}
            </span>
          ))}
        </span>
      </div>
      <p className="mb-3 text-[11.5px] leading-[1.6] text-soft">
        막대는 그 숙련도를 받은 문항 수이고, 태그 옆 배지는 <strong className="font-semibold">현재</strong>{" "}
        숙련도입니다. 서술형은 채점 AI가 답변 내용을 보고 판정하며 그 근거를 함께 남깁니다.
      </p>
      <ul className="flex flex-col gap-3.5">
        {rows.map((row) => (
          <li key={row.category} className="rounded-[12px] border border-line-soft px-3.5 py-3">
            <div className="mb-2 flex items-baseline justify-between gap-2">
              <span className="text-[13.5px] font-medium text-ink">
                {categoryLabel(row.category)}
              </span>
              <span className="font-mono text-[11.5px] text-secondary">{total(row)}문항</span>
            </div>
            <LevelBar category={row} />
            {row.tags.length > 0 && (
              <ul className="mt-2.5">
                {row.tags.map((tag) => (
                  <TagRow key={tag.tagId} tag={tag} />
                ))}
              </ul>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
