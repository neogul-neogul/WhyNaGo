"use client";

import { useState } from "react";
import type { GrowthDatum } from "@/types";
import { gradeColor } from "@/lib/badges";
import { palette } from "@/lib/tokens";
import Card from "@/components/ui/Card";

const GRADE_VAL: Record<string, number> = { A: 4, B: 3, C: 2, D: 1 };
const X_LABELS = ["1주", "2주", "3주", "4주", "5주", "6주", "7주", "8주"];
const G_LEFT = 54, G_RIGHT = 656, G_TOP = 28, G_BOTTOM = 188;

// 카테고리별 성장 곡선 차트 (카테고리 선택 상태는 이 섹션 안에서만 쓰인다)
export default function GrowthChart({ data }: { data: GrowthDatum[] }) {
  const [cat, setCat] = useState(data[0]?.cat ?? "");
  const g = data.find((x) => x.cat === cat) ?? data[0];
  const n = g.grades.length;
  const color = gradeColor(g.grades[n - 1]);

  const pts = g.grades.map((gr, i) => ({
    x: G_LEFT + (i / (n - 1)) * (G_RIGHT - G_LEFT),
    y: G_BOTTOM - ((GRADE_VAL[gr] - 1) / 3) * (G_BOTTOM - G_TOP),
    grade: gr,
  }));
  const pointsStr = pts.map((p) => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");
  const areaStr = `${G_LEFT},${G_BOTTOM} ${pointsStr} ${G_RIGHT},${G_BOTTOM}`;
  const gridlines = ["A", "B", "C", "D"].map((gr) => ({
    y: (G_BOTTOM - ((GRADE_VAL[gr] - 1) / 3) * (G_BOTTOM - G_TOP)).toFixed(1),
    label: gr,
  }));

  return (
    <div>
      <div className="mb-[13px] text-[13px] font-semibold text-muted">카테고리별 성장 곡선</div>
      <Card className="flex flex-col gap-[18px] px-6 py-[22px]">
        <div className="flex items-center justify-between gap-3">
          <span className="text-[17px] font-bold tracking-[-0.2px]" style={{ color }}>
            {g.cat}
          </span>
          <span className="font-mono text-[14px] font-bold text-secondary">
            {g.grades[0]} → {g.grades[n - 1]}
          </span>
        </div>
        <svg viewBox="0 0 700 210" className="h-auto w-full overflow-visible">
          {gridlines.map((gl, i) => (
            <g key={i}>
              <line x1="54" y1={gl.y} x2="656" y2={gl.y} stroke={palette.lineSoft} strokeWidth="1" />
              <text x="34" y={gl.y} textAnchor="end" dominantBaseline="middle" fontSize="13" fontWeight="700" fill={palette.axis}>{gl.label}</text>
            </g>
          ))}
          <polygon points={areaStr} fill={color} opacity="0.07" />
          <polyline points={pointsStr} fill="none" stroke={color} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
          {pts.map((p, i) => (
            <g key={i}>
              <circle cx={p.x.toFixed(1)} cy={p.y.toFixed(1)} r="2.5" fill="#fff" stroke={color} strokeWidth="2" />
              <text x={p.x.toFixed(1)} y={(p.y - 12).toFixed(1)} textAnchor="middle" fontSize="12.5" fontWeight="700" fill={color}>{p.grade}</text>
            </g>
          ))}
          {pts.map((p, i) => (
            <text key={`x${i}`} x={p.x.toFixed(1)} y="206" textAnchor="middle" fontSize="12" fontWeight="500" fill={palette.placeholder}>{X_LABELS[i] ?? ""}</text>
          ))}
        </svg>
        <div className="flex flex-wrap gap-2 border-t border-line-soft pt-4">
          {data.map((x) => {
            const active = x.cat === cat;
            return (
              <button
                key={x.cat}
                type="button"
                onClick={() => setCat(x.cat)}
                className={`rounded-[9px] border px-4 py-2 text-[13px] font-semibold transition-all ${
                  active
                    ? "border-ink bg-ink text-white"
                    : "border-line-input bg-white text-secondary"
                }`}
              >
                {x.cat}
              </button>
            );
          })}
        </div>
      </Card>
    </div>
  );
}
