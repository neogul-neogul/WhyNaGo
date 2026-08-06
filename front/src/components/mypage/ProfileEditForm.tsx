"use client";

import type { Profile } from "@/types";
import { POSITION_LABELS } from "@/lib/user";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import Input from "@/components/ui/Input";

const POSITION_OPTIONS = Object.values(POSITION_LABELS);
const FIELD_CLASS =
  "w-full rounded-[11px] border border-line-input bg-subtle px-[15px] py-[13px] text-sm text-ink outline-none focus:border-ink";

// 프로필 수정 폼
export default function ProfileEditForm({
  draft,
  onChange,
  onCancel,
  onSave,
  saving,
}: {
  draft: Profile;
  onChange: (key: keyof Profile, value: string) => void;
  onCancel: () => void;
  onSave: () => void;
  saving?: boolean;
}) {
  return (
    <Card className="flex flex-col gap-4 px-[26px] py-6">
      <Field label="닉네임">
        <Input value={draft.nickname} onChange={(e) => onChange("nickname", e.target.value)} />
      </Field>
      <Field label="이메일">
        <Input value={draft.email} disabled className="cursor-not-allowed opacity-60" />
      </Field>
      <Field label="직무">
        <select
          value={draft.job}
          onChange={(e) => onChange("job", e.target.value)}
          className={FIELD_CLASS}
        >
          {POSITION_OPTIONS.map((label) => (
            <option key={label} value={label}>
              {label}
            </option>
          ))}
        </select>
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
        <Button variant="muted" size="md" onClick={onCancel} disabled={saving}>
          취소
        </Button>
        <Button size="md" onClick={onSave} disabled={saving}>
          {saving ? "저장 중…" : "저장"}
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
