import type { CategoryProgressResponse } from "@/types";
import { categoryLabel } from "@/lib/progress";
import { palette } from "@/lib/tokens";

const CORRECT_COLOR = palette.success;
const WRONG_COLOR = palette.warning;

function widths(category: CategoryProgressResponse) {
  if (category.totalCount <= 0) return { correct: 0, wrong: 0 };
  const correct = Math.min((category.correctCount / category.totalCount) * 100, 100);
  const wrong = Math.min(
    ((category.solvedCount - category.correctCount) / category.totalCount) * 100,
    100 - correct,
  );
  return { correct, wrong };
}

// 카테고리별 진척도 — 문제은행 전체 문항 대비 맞힌 문항과 풀었지만 못 맞힌 문항을 함께 보여준다.
export default function CategoryProgressList({
  categories,
}: {
  categories: CategoryProgressResponse[];
}) {
  const rows = [...categories].sort(
    (a, b) => b.correctCount - a.correctCount || b.solvedCount - a.solvedCount,
  );

  return (
    <div>
      <div className="mb-2 flex flex-wrap items-baseline justify-between gap-2">
        <span className="text-[12.5px] font-bold text-ink">카테고리별 진척도</span>
        <span className="flex items-center gap-3 text-[11px] text-secondary">
          <span className="flex items-center gap-1.5">
            <span
              className="inline-block h-2 w-2 rounded-[2px]"
              style={{ background: CORRECT_COLOR }}
            />
            맞힘
          </span>
          <span className="flex items-center gap-1.5">
            <span
              className="inline-block h-2 w-2 rounded-[2px]"
              style={{ background: WRONG_COLOR }}
            />
            풀었지만 오답
          </span>
        </span>
      </div>
      <ul>
        {rows.map((row) => {
          const { correct, wrong } = widths(row);
          return (
            <li
              key={row.category}
              className="grid grid-cols-[84px_1fr_auto] items-center gap-[14px] rounded-[7px] border-b border-line-soft px-1 py-2.5 transition-colors hover:bg-subtle"
            >
              <span className="text-[13.5px] font-medium text-ink">
                {categoryLabel(row.category)}
              </span>
              <span
                className="flex h-2.5 gap-[2px] overflow-hidden rounded-[5px] bg-neutral"
                title={`전체 ${row.totalCount}문제 중 ${row.solvedCount}문제 풀이, ${row.correctCount}문제 맞힘`}
              >
                <span
                  className="rounded-[4px]"
                  style={{ width: `${correct.toFixed(1)}%`, background: CORRECT_COLOR }}
                />
                <span
                  className="rounded-[4px]"
                  style={{ width: `${wrong.toFixed(1)}%`, background: WRONG_COLOR }}
                />
              </span>
              <span className="flex justify-end gap-3 font-mono text-[11.5px] text-secondary">
                <span className="whitespace-nowrap">
                  <span className="font-bold text-ink">{row.correctCount}</span>
                  {" / "}
                  {row.totalCount}
                </span>
                <span className="whitespace-nowrap">{row.score}점</span>
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
