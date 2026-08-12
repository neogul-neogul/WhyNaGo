import type { CategoryProgressResponse } from "@/types";
import { palette } from "@/lib/tokens";
import { categoryLabel, radarMaxScore, SERIES_FILL, SERIES_LINE } from "@/lib/progress";

const CX = 210;
const CY = 200;
const R = 150;
const LABEL_GAP = 26;
const RINGS = [0.25, 0.5, 0.75, 1];

function pointAt(index: number, count: number, radius: number) {
  const angle = ((-90 + (index * 360) / count) * Math.PI) / 180;
  return { x: CX + Math.cos(angle) * radius, y: CY + Math.sin(angle) * radius };
}

// 카테고리별 점수 레이더 — 축 하나가 카테고리 하나, 도형은 "현재 내 점수" 한 계열이다.
export default function CategoryRadar({ categories }: { categories: CategoryProgressResponse[] }) {
  const maxScore = radarMaxScore(categories);
  const count = categories.length;

  const vertices = categories.map((c, i) => pointAt(i, count, (c.score / maxScore) * R));
  const polygon = vertices.map((p) => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");

  return (
    <div className="flex flex-col gap-1.5">
      <svg viewBox="-40 -6 500 412" className="block w-full" role="img" aria-label="카테고리별 점수 레이더">
        {RINGS.map((ratio) => (
          <circle
            key={ratio}
            cx={CX}
            cy={CY}
            r={(R * ratio).toFixed(1)}
            fill="none"
            stroke={ratio === 1 ? palette.lineStrong : palette.lineSoft}
            strokeWidth="1"
          />
        ))}

        {categories.map((c, i) => {
          const end = pointAt(i, count, R);
          return (
            <line
              key={c.category}
              x1={CX}
              y1={CY}
              x2={end.x.toFixed(1)}
              y2={end.y.toFixed(1)}
              stroke={palette.lineSoft}
              strokeWidth="1"
            />
          );
        })}

        <polygon points={polygon} fill={SERIES_FILL} stroke={SERIES_LINE} strokeWidth="2" />

        {categories.map((c, i) => (
          <circle
            key={c.category}
            cx={vertices[i].x.toFixed(1)}
            cy={vertices[i].y.toFixed(1)}
            r="4.5"
            fill={SERIES_LINE}
            stroke="#fff"
            strokeWidth="2"
          >
            <title>{`${categoryLabel(c.category)} ${c.score}점`}</title>
          </circle>
        ))}

        {categories.map((c, i) => {
          const label = pointAt(i, count, R + LABEL_GAP);
          const anchor =
            Math.abs(label.x - CX) < 12 ? "middle" : label.x > CX ? "start" : "end";
          return (
            <text
              key={c.category}
              x={label.x.toFixed(1)}
              y={(label.y + 4).toFixed(1)}
              textAnchor={anchor}
              fontSize="12.5"
              fontWeight="500"
              fill={palette.body}
            >
              {categoryLabel(c.category)}
            </text>
          );
        })}
      </svg>
      <p className="text-center text-[11px] text-secondary">바깥 원 = {maxScore}점</p>
    </div>
  );
}
