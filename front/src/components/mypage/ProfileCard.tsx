"use client";

import type { Profile } from "@/types";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

// 프로필 요약 카드 (아바타 + 닉네임/이메일/직무 + 수정 버튼)
export default function ProfileCard({
  profile,
  editing,
  onEdit,
}: {
  profile: Profile;
  editing: boolean;
  onEdit: () => void;
}) {
  return (
    <Card className="px-7 py-[26px]">
      <div className="flex items-center gap-[18px]">
        <div className="flex h-[66px] w-[66px] flex-shrink-0 items-center justify-center rounded-full bg-ink">
          <span className="text-[26px] font-bold text-white">
            {profile.nickname.slice(0, 1)}
          </span>
        </div>
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <span className="text-[20px] font-bold tracking-[-0.3px]">{profile.nickname}</span>
          <span className="text-[13.5px] text-soft">{profile.email}</span>
          <Badge tone="accent" className="mt-[3px]">{profile.job}</Badge>
        </div>
        {!editing && (
          <Button size="md" onClick={onEdit} className="flex-shrink-0">
            프로필 수정
          </Button>
        )}
      </div>
    </Card>
  );
}
