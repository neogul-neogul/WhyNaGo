// 콘텐츠 카드 컨테이너 (docs/DESIGN.md §6.4) — 패딩은 사용처에서 지정한다
export default function Card({
  className = "",
  children,
}: {
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <div className={`rounded-[16px] border border-line-card bg-white ${className}`}>
      {children}
    </div>
  );
}

// 카드 상단 헤더 스트립 (연한 배경 + 하단 구분선)
export function CardHeader({
  className = "",
  children,
}: {
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <div
      className={`flex items-center border-b border-line-card bg-subtle px-[22px] py-[13px] ${className}`}
    >
      {children}
    </div>
  );
}
