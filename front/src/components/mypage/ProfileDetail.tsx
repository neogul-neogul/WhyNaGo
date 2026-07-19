import type { Profile } from "@/types";
import Card from "@/components/ui/Card";

// 프로필 상세 보기 (읽기 전용)
export default function ProfileDetail({ profile }: { profile: Profile }) {
  return (
    <Card className="px-[26px] py-2">
      <DetailRow label="닉네임" value={profile.nickname} border />
      <DetailRow label="이메일" value={profile.email} border />
      <DetailRow label="최소 학습 목표" value={`매일 최소 ${profile.goal || "0"}개`} />
    </Card>
  );
}

function DetailRow({ label, value, border }: { label: string; value: string; border?: boolean }) {
  return (
    <div className={`flex flex-col gap-[3px] py-[18px] ${border ? "border-b border-line-soft" : ""}`}>
      <span className="text-[12.5px] font-semibold text-soft">{label}</span>
      <span className="text-[14.5px] font-semibold text-ink">{value}</span>
    </div>
  );
}
