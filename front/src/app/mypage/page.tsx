"use client";

import { useState } from "react";
import type { Profile } from "@/types";
import { defaultProfile, mypageStats } from "@/mocks/mypage";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

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
  const setField = (k: keyof Profile) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setDraft((d) => ({ ...d, [k]: e.target.value }));

  const inputClass =
    "w-full rounded-[11px] border border-line-input bg-subtle px-[15px] py-3 text-sm text-ink outline-none";

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="마이페이지" subtitle="프로필 정보를 확인하고 수정할 수 있습니다" />
      <PageBody>
        <div className="flex max-w-[720px] flex-col gap-[18px]">
          {/* 프로필 카드 */}
          <Card className="px-7 py-[26px]">
            <div className="flex items-center gap-[18px]">
              <div className="flex h-[66px] w-[66px] flex-shrink-0 items-center justify-center rounded-full bg-ink">
                <span className="text-[26px] font-bold text-white">{profile.nickname.slice(0, 1)}</span>
              </div>
              <div className="flex min-w-0 flex-1 flex-col gap-1">
                <span className="text-[20px] font-bold tracking-[-0.3px]">{profile.nickname}</span>
                <span className="text-[13.5px] text-soft">{profile.email}</span>
                <Badge tone="accent" className="mt-[3px]">{profile.job}</Badge>
              </div>
              {!editing && (
                <Button size="md" onClick={startEdit} className="flex-shrink-0">
                  프로필 수정
                </Button>
              )}
            </div>
          </Card>

          {/* 통계 */}
          <div className="grid grid-cols-4 gap-3">
            {mypageStats.map((st) => (
              <div key={st.label} className="flex flex-col gap-1.5 rounded-[14px] border border-line-card bg-white px-5 py-[18px]">
                <span className="text-xs font-semibold text-soft">{st.label}</span>
                <span className="text-[24px] font-bold tracking-[-0.5px]">
                  {st.value}
                  {st.unit && <span className="ml-0.5 text-[14px] font-semibold text-placeholder">{st.unit}</span>}
                </span>
              </div>
            ))}
          </div>

          {/* 상세: 보기 모드 */}
          {!editing ? (
            <Card className="px-[26px] py-2">
              <DetailRow label="닉네임" value={profile.nickname} border />
              <DetailRow label="이메일" value={profile.email} border />
              <DetailRow label="최소 학습 목표" value={`매일 최소 ${profile.goal || "0"}개`} />
            </Card>
          ) : (
            <Card className="flex flex-col gap-4 px-[26px] py-6">
              <Field label="닉네임">
                <input value={draft.nickname} onChange={setField("nickname")} className={inputClass} />
              </Field>
              <Field label="이메일">
                <input value={draft.email} onChange={setField("email")} className={inputClass} />
              </Field>
              <Field label="직무">
                <input value={draft.job} onChange={setField("job")} className={inputClass} />
              </Field>
              <Field label="최소 학습 목표 문제 개수">
                <div className="flex items-center gap-2.5">
                  <input
                    type="number"
                    min={1}
                    value={draft.goal}
                    onChange={setField("goal")}
                    className="w-[120px] rounded-[11px] border border-line-input bg-subtle px-[15px] py-3 text-sm text-ink outline-none"
                  />
                  <span className="text-[14px] text-secondary">개 / 일</span>
                </div>
              </Field>
              <div className="flex justify-end gap-2.5 pt-0.5">
                <Button variant="muted" size="md" onClick={() => setEditing(false)}>
                  취소
                </Button>
                <Button size="md" onClick={save}>
                  저장
                </Button>
              </div>
            </Card>
          )}
        </div>
      </PageBody>
    </main>
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

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-[7px]">
      <span className="text-[12.5px] font-semibold text-muted">{label}</span>
      {children}
    </div>
  );
}
