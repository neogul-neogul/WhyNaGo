import { palette } from "@/lib/tokens";
import { adminDailySolveCounts, adminSolveChartLabels } from "@/mocks/admin";

// 차트 좌표계 (viewBox 기준)
const LEFT = 60;
const RIGHT = 686;
const BASE_Y = 220;
const PLOT_HEIGHT = 180;
const MIN_VALUE = 400;
const VALUE_RANGE = 1200;
const Y_TICKS = [1600, 1200, 800, 400];

// 일별 풀이 수 추이 (더미) — 값이 좌표로 바뀌므로 SVG 속성에는 palette를 쓴다
export default function SolveTrendChart() {
  const step = (RIGHT - LEFT) / (adminDailySolveCounts.length - 1);
  const points = adminDailySolveCounts.map((value, i) => ({
    x: Math.round((LEFT + i * step) * 10) / 10,
    y: Math.round((BASE_Y - ((value - MIN_VALUE) / VALUE_RANGE) * PLOT_HEIGHT) * 10) / 10,
  }));

  return (
    <svg viewBox="0 0 700 250" className="h-auto w-full overflow-visible">
      {Y_TICKS.map((tick, i) => {
        const y = 40 + i * 60;
        return (
          <g key={tick}>
            <line x1={LEFT} y1={y} x2={RIGHT} y2={y} stroke={palette.lineSoft} strokeWidth="1" />
            <text
              x={48}
              y={y}
              textAnchor="end"
              dominantBaseline="middle"
              fontSize="12"
              fontWeight="500"
              fill={palette.placeholder}
              className="font-mono"
            >
              {tick}
            </text>
          </g>
        );
      })}

      <polyline
        points={points.map((p) => `${p.x},${p.y}`).join(" ")}
        fill="none"
        stroke={palette.ink}
        strokeWidth="2.4"
        strokeLinejoin="round"
      />
      {points.map((p) => (
        <circle key={p.x} cx={p.x} cy={p.y} r="3.6" fill={palette.ink} />
      ))}

      {adminSolveChartLabels.map((label) => (
        <text
          key={label.label}
          x={points[label.index].x}
          y={244}
          textAnchor="middle"
          fontSize="12"
          fontWeight="500"
          fill={palette.placeholder}
          className="font-mono"
        >
          {label.label}
        </text>
      ))}
    </svg>
  );
}
