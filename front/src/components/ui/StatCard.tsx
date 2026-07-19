// 라벨 + 수치 지표 카드 (docs/DESIGN.md §7) — 수치 영역은 children으로 자유롭게 구성한다
export default function StatCard({
  label,
  className = "",
  children,
}: {
  label: string;
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <div
      className={`flex flex-col gap-[7px] rounded-[14px] border border-line-card bg-white px-5 py-[18px] ${className}`}
    >
      <span className="text-[12.5px] font-medium text-muted">{label}</span>
      {children}
    </div>
  );
}
