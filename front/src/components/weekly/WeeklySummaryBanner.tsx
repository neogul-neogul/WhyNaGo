// 이번 주 학습 요약 다크 배너
export default function WeeklySummaryBanner({
  range,
  days,
  rate,
}: {
  range: string;
  days: number;
  rate: number;
}) {
  return (
    <div className="flex items-center justify-between gap-5 rounded-[16px] bg-ink px-7 py-6 text-white">
      <div className="flex flex-col gap-[5px]">
        <span className="text-[12.5px] text-placeholder">{range}</span>
        <span className="text-[20px] font-bold">이번 주 학습 요약</span>
      </div>
      <div className="flex gap-7">
        <div className="flex flex-col items-center gap-0.5">
          <span className="font-mono text-[30px] font-bold">{days}</span>
          <span className="text-[11.5px] text-placeholder">학습일 / 7</span>
        </div>
        <div className="w-px bg-white/[0.12]" />
        <div className="flex flex-col items-center gap-0.5">
          <span className="font-mono text-[30px] font-bold text-success-bright">{rate}%</span>
          <span className="text-[11.5px] text-placeholder">평균 정답률</span>
        </div>
      </div>
    </div>
  );
}
