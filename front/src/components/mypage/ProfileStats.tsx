import StatCard from "@/components/ui/StatCard";

// 마이페이지 요약 통계 그리드
export default function ProfileStats({
  stats,
}: {
  stats: { label: string; value: string; unit: string }[];
}) {
  return (
    <div className="grid grid-cols-4 gap-3">
      {stats.map((st) => (
        <StatCard key={st.label} label={st.label}>
          <span className="text-[24px] font-bold tracking-[-0.5px]">
            {st.value}
            {st.unit && (
              <span className="ml-0.5 text-[14px] font-semibold text-placeholder">
                {st.unit}
              </span>
            )}
          </span>
        </StatCard>
      ))}
    </div>
  );
}
