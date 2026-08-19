// 관리자 화면 로고. 공통 헤더(Header.tsx)의 로고 마크를 그대로 쓰고 크기만 화면에 맞춘다.
export default function AdminLogo({ size = "sm" }: { size?: "sm" | "lg" }) {
  return (
    <div
      className={`flex flex-shrink-0 items-center justify-center bg-ink ${
        size === "lg" ? "h-[72px] w-[72px] rounded-[16px]" : "h-8 w-8 rounded-[9px]"
      }`}
    >
      <span
        className={`font-mono font-bold tracking-[-0.5px] text-white ${
          size === "lg" ? "text-[30px]" : "text-[15px]"
        }`}
      >
        &lt;/&gt;
      </span>
    </div>
  );
}
