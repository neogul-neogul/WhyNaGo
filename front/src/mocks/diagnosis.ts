import type { CatStat, GrowthDatum } from "@/types";

// 모의진단 — 카테고리별 등급/정답률 (더미)
export const catStatsData: CatStat[] = [
  { name: "DB", acc: 82, count: 64, grade: "A", change: "B→A" },
  { name: "네트워크", acc: 71, count: 58, grade: "B", change: "C→B" },
  { name: "알고리즘", acc: 66, count: 90, grade: "B", change: "C→B" },
  { name: "자료구조", acc: 74, count: 52, grade: "B", change: "B→B" },
  { name: "운영체제", acc: 63, count: 47, grade: "C", change: "D→C" },
  { name: "디자인패턴", acc: 58, count: 31, grade: "C", change: "C→C" },
  { name: "언어", acc: 79, count: 43, grade: "A", change: "B→A" },
];

// 진척도 — 카테고리별 성장 곡선 (주차별 등급, 더미)
export const growthData: GrowthDatum[] = [
  { cat: "네트워크", grades: ["D", "C", "C", "B", "B", "B", "B", "B"] },
  { cat: "운영체제", grades: ["D", "D", "C", "C", "C", "B", "B", "B"] },
  { cat: "DB", grades: ["C", "C", "B", "B", "B", "A", "A", "A"] },
  { cat: "알고리즘", grades: ["D", "C", "C", "C", "B", "B", "B", "A"] },
  { cat: "자료구조", grades: ["D", "C", "C", "B", "B", "B", "B", "B"] },
  { cat: "언어", grades: ["C", "B", "B", "B", "B", "A", "A", "A"] },
  { cat: "디자인 패턴", grades: ["D", "D", "C", "C", "B", "B", "B", "A"] },
];
