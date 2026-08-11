const CONDITIONS = ["오늘 학습 기록이 없으면 알림 발송"];

// 알림 발송 조건 안내 배너
export default function SendConditions() {
  return (
    <div className="flex flex-col gap-[11px] rounded-[13px] border border-line-card bg-subtle px-6 py-5">
      <span className="text-[13px] font-semibold text-muted">발송 조건</span>
      <div className="flex flex-col gap-2">
        {CONDITIONS.map((c) => (
          <div key={c} className="flex items-start gap-2.5 text-[13px] leading-[1.5] text-dim">
            <span className="text-alert">•</span>
            {c}
          </div>
        ))}
      </div>
    </div>
  );
}
