"use client";

import { useState } from "react";
import { progressMetrics } from "@/mocks/progress";
import { growthData } from "@/mocks/diagnosis";
import { gradeColor } from "@/lib/badges";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";

const GRADE_VAL: Record<string, number> = { A: 4, B: 3, C: 2, D: 1 };
const X_LABELS = ["1주", "2주", "3주", "4주", "5주", "6주", "7주", "8주"];
const G_LEFT = 54, G_RIGHT = 656, G_TOP = 28, G_BOTTOM = 188;

export default function ProgressPage() {
  const [cat, setCat] = useState("네트워크");
  const g = growthData.find((x) => x.cat === cat) ?? growthData[0];
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
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="진척도" subtitle="누적 학습 상태와 카테고리별 성장을 봅니다" />
      <PageBody>
        <div className="flex flex-col gap-[22px]">
          {/* 지표 */}
          <div className="grid grid-cols-3 gap-[13px]">
            {progressMetrics.map((m) => (
              <div key={m.label} className="flex flex-col gap-[7px] rounded-[13px] border border-[#ECECE8] bg-white px-5 py-[18px]">
                <span className="text-[12.5px] font-medium text-[#8A8A80]">{m.label}</span>
                <div className="flex items-baseline gap-[5px]">
                  <span className="font-mono text-[26px] font-bold" style={{ color: m.color }}>{m.value}</span>
                  <span className="text-[12.5px] text-[#A8A8A0]">{m.unit}</span>
                </div>
              </div>
            ))}
          </div>

          {/* 성장 곡선 */}
          <div>
            <div className="mb-[13px] text-[13px] font-semibold text-[#8A8A80]">카테고리별 성장 곡선</div>
            <div className="flex flex-col gap-[18px] rounded-[16px] border border-[#ECECE8] bg-white px-6 py-[22px]">
              <div className="flex items-center justify-between gap-3">
                <span className="text-[17px] font-bold tracking-[-0.2px]" style={{ color }}>{g.cat}</span>
                <span className="font-mono text-[14px] font-bold text-[#6B6B62]">
                  {g.grades[0]} → {g.grades[n - 1]}
                </span>
              </div>
              <svg viewBox="0 0 700 210" className="h-auto w-full overflow-visible">
                {gridlines.map((gl, i) => (
                  <g key={i}>
                    <line x1="54" y1={gl.y} x2="656" y2={gl.y} stroke="#F0F0EC" strokeWidth="1" />
                    <text x="34" y={gl.y} textAnchor="end" dominantBaseline="middle" fontSize="13" fontWeight="700" fill="#B0B0A6">{gl.label}</text>
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
                  <text key={`x${i}`} x={p.x.toFixed(1)} y="206" textAnchor="middle" fontSize="12" fontWeight="500" fill="#A8A8A0">{X_LABELS[i] ?? ""}</text>
                ))}
              </svg>
              <div className="flex flex-wrap gap-2 border-t border-[#F2F2EE] pt-4">
                {growthData.map((x) => {
                  const active = x.cat === cat;
                  return (
                    <button
                      key={x.cat}
                      type="button"
                      onClick={() => setCat(x.cat)}
                      className="rounded-[9px] px-4 py-2 text-[13px] font-semibold transition-all"
                      style={{
                        border: `1px solid ${active ? "#1C1C1A" : "#E0E0DA"}`,
                        background: active ? "#1C1C1A" : "#fff",
                        color: active ? "#fff" : "#6B6B62",
                      }}
                    >
                      {x.cat}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </PageBody>
    </main>
  );
}
