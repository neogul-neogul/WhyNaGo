"use client";

import { useEffect, useState } from "react";
import type { Profile } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchMyProfile, updateDailyGoal } from "@/lib/user";
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
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 닉네임·이메일·직무·소개는 아직 백엔드에 없어 더미를 유지하고, 최소 학습 목표만 실제 값으로 채운다
  useEffect(() => {
    let cancelled = false;
    fetchMyProfile()
      .then((result) => {
        if (!cancelled) setProfile((p) => ({ ...p, goal: String(result.dailyGoal) }));
      })
      .catch(() => {
        // 조회 실패 시 더미 기본값을 그대로 보여준다
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const startEdit = () => {
    setError(null);
    setDraft(profile);
    setEditing(true);
  };

  const save = async () => {
    if (saving) return;
    setSaving(true);
    setError(null);
    try {
      const result = await updateDailyGoal(Number(draft.goal));
      setProfile({ ...draft, goal: String(result.dailyGoal) });
      setEditing(false);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "저장에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setSaving(false);
    }
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
            <>
              {error && (
                <div className="text-[13px] font-semibold text-danger">{error}</div>
              )}
              <ProfileEditForm
                draft={draft}
                onChange={(k, v) => setDraft((d) => ({ ...d, [k]: v }))}
                onCancel={() => setEditing(false)}
                onSave={save}
                saving={saving}
              />
            </>
          )}
        </div>
      </PageBody>
    </main>
  );
}
