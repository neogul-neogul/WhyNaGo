import type { GrassDay, RecordItem } from "@/types";

// 잔디 레벨별 색상 (GitHub 스타일)
export const grassColors = ["#EBEDF0", "#9BE9A8", "#40C463", "#30A14E", "#216E39"];

// 잔디 데이터 (결정적 시드 PRNG로 생성 → SSR/CSR 일치) — 더미
export const grassWeeksData: { days: GrassDay[] }[] = (() => {
  const counts = [0, 3, 7, 13, 23];
  let seed = 7;
  const rnd = () => {
    seed = (seed * 1103515245 + 12345) & 0x7fffffff;
    return seed / 0x7fffffff;
  };
  const weeks: { days: GrassDay[] }[] = [];
  for (let w = 0; w < 53; w++) {
    const days: GrassDay[] = [];
    const recency = w / 52;
    for (let d = 0; d < 7; d++) {
      let level = 0;
      if (rnd() < 0.32 + recency * 0.45) {
        level = 1 + Math.floor(rnd() * (1 + recency * 3.4));
        if (level > 4) level = 4;
      }
      const count = level === 0 ? 0 : counts[level] + Math.floor(rnd() * 4);
      days.push({ level, color: grassColors[level], count });
    }
    weeks.push({ days });
  }
  return weeks;
})();

// 잔디 레벨 안내
export const levelGuide = [
  { color: grassColors[0], label: "학습 없음" },
  { color: grassColors[1], label: "학습 1개 완료" },
  { color: grassColors[2], label: "문제 5개 이상" },
  { color: grassColors[3], label: "문제 10개 이상 · 오답 복습" },
  { color: grassColors[4], label: "문제 20개 이상 · 면접 완료" },
];

// 최근 학습 기록 — 더미
export const recordsData: RecordItem[] = [
  { date: "06.25", time: "18분", method: "문제 풀이", cats: ["네트워크", "DB"], solved: 12, correct: 9, wrong: 3, score: 24 },
  { date: "06.24", time: "9분", method: "1일 1면접", cats: ["운영체제"], solved: 1, correct: 1, wrong: 0, score: 12 },
  { date: "06.24", time: "14분", method: "오답 복습", cats: ["알고리즘"], solved: 6, correct: 5, wrong: 1, score: 16 },
  { date: "06.23", time: "22분", method: "모의 진단", cats: ["전체"], solved: 20, correct: 13, wrong: 7, score: 30 },
  { date: "06.22", time: "11분", method: "카테고리별", cats: ["자료구조"], solved: 10, correct: 8, wrong: 2, score: 20 },
];
