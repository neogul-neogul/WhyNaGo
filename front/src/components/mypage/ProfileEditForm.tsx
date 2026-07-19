"use client";

import type { Profile } from "@/types";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import Input from "@/components/ui/Input";

// 프로필 수정 폼
export default function ProfileEditForm({
  draft,
  onChange,
  onCancel,
  onSave,
}: {
  draft: Profile;
  onChange: (key: keyof Profile, value: string) => void;
  onCancel: () => void;
  onSave: () => void;
}) {
  return (
    <Card className="flex flex-col gap-4 px-[26px] py-6">
      <Field label="닉네임">
        <Input value={draft.nickname} onChange={(e) => onChange("nickname", e.target.value)} />
      </Field>
      <Field label="이메일">
        <Input value={draft.email} onChange={(e) => onChange("email", e.target.value)} />
      </Field>
      <Field label="직무">
        <Input value={draft.job} onChange={(e) => onChange("job", e.target.value)} />
      </Field>
      <Field label="최소 학습 목표 문제 개수">
        <div className="flex items-center gap-2.5">
          <div className="w-[120px]">
            <Input
              type="number"
              min={1}
              value={draft.goal}
              onChange={(e) => onChange("goal", e.target.value)}
            />
          </div>
          <span className="text-[14px] text-secondary">개 / 일</span>
        </div>
      </Field>
      <div className="flex justify-end gap-2.5 pt-0.5">
        <Button variant="muted" size="md" onClick={onCancel}>
          취소
        </Button>
        <Button size="md" onClick={onSave}>
          저장
        </Button>
      </div>
    </Card>
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
