"use client";

import { useState } from "react";
import type { Profile } from "@/types";
import { defaultProfile, mypageStats } from "@/mocks/mypage";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import ProfileCard from "@/components/mypage/ProfileCard";
import ProfileStats from "@/components/mypage/ProfileStats";
import ProfileDetail from "@/components/mypage/ProfileDetail";
import ProfileEditForm from "@/components/mypage/ProfileEditForm";

export default function MypagePage() {
  const [profile, setProfile] = useState<Profile>(defaultProfile);
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState<Profile>(defaultProfile);

  const startEdit = () => {
    setDraft(profile);
    setEditing(true);
  };
  const save = () => {
    setProfile(draft);
    setEditing(false);
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="마이페이지" subtitle="프로필 정보를 확인하고 수정할 수 있습니다" />
      <PageBody>
        <div className="flex max-w-[720px] flex-col gap-[18px]">
          <ProfileCard profile={profile} editing={editing} onEdit={startEdit} />
          <ProfileStats stats={mypageStats} />
          {!editing ? (
            <ProfileDetail profile={profile} />
          ) : (
            <ProfileEditForm
              draft={draft}
              onChange={(k, v) => setDraft((d) => ({ ...d, [k]: v }))}
              onCancel={() => setEditing(false)}
              onSave={save}
            />
          )}
        </div>
      </PageBody>
    </main>
  );
}
