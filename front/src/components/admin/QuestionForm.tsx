"use client";

import { useState } from "react";
import type { AdminQuestionForm, QuestionCategory, QuestionDifficulty } from "@/types";
import { ADMIN_CATEGORIES, ADMIN_DIFFICULTIES } from "@/mocks/admin";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import Input from "@/components/ui/Input";
import FilterSelect from "@/components/admin/FilterSelect";

const TEXTAREA_CLASS =
  "block w-full resize-y rounded-[11px] border border-line-input bg-subtle px-4 py-3.5 text-sm leading-[1.7] text-ink outline-none focus:border-ink";

/**
 * 문제 등록 · 수정 폼 (더미).
 * 저장은 서버로 나가지 않고 저장 완료 안내만 띄운다 — 어드민 문제 등록 API가 아직 없다.
 */
export default function QuestionForm({
  initial,
  onCancel,
}: {
  initial: AdminQuestionForm;
  onCancel: () => void;
}) {
  const [form, setForm] = useState(initial);
  const [tagDraft, setTagDraft] = useState("");
  const [saved, setSaved] = useState(false);

  const update = (patch: Partial<AdminQuestionForm>) => {
    setForm((f) => ({ ...f, ...patch }));
    setSaved(false);
  };

  const updateOption = (index: number, patch: { text?: string; explanation?: string }) =>
    update({
      options: form.options.map((o, i) => (i === index ? { ...o, ...patch } : o)),
    });

  const addTag = () => {
    const value = tagDraft.trim();
    if (!value) return;
    update({ tags: [...form.tags, value.startsWith("#") ? value : `#${value}`] });
    setTagDraft("");
  };

  return (
    <div className="flex w-full flex-col gap-[18px]">
      <Card className="flex flex-col gap-4 p-6">
        <span className="text-[13px] font-semibold text-muted">기본 정보</span>

        <div className="grid grid-cols-2 gap-3.5">
          <Field label="카테고리">
            <FilterSelect
              value={form.category}
              options={ADMIN_CATEGORIES}
              onChange={(v) => update({ category: v as QuestionCategory })}
            />
          </Field>
          <Field label="난이도">
            <FilterSelect
              value={form.difficulty}
              options={ADMIN_DIFFICULTIES}
              onChange={(v) => update({ difficulty: v as QuestionDifficulty })}
            />
          </Field>
        </div>

        <Field label="태그">
          <div className="flex flex-wrap items-center gap-2 rounded-[11px] border border-line-input bg-subtle px-3.5 py-3">
            {form.tags.map((tag, i) => (
              <span
                key={tag}
                className="flex items-center gap-1.5 rounded-[5px] bg-neutral px-2.5 py-[5px] text-[11.5px] font-medium text-secondary"
              >
                {tag}
                <button
                  type="button"
                  aria-label={`${tag} 삭제`}
                  onClick={() => update({ tags: form.tags.filter((_, j) => j !== i) })}
                  className="cursor-pointer text-xs text-soft transition-colors hover:text-ink"
                >
                  ✕
                </button>
              </span>
            ))}
            <input
              value={tagDraft}
              onChange={(e) => setTagDraft(e.target.value)}
              onKeyDown={(e) => {
                if (e.key !== "Enter") return;
                e.preventDefault();
                addTag();
              }}
              placeholder="태그 입력 후 Enter"
              className="min-w-[140px] flex-1 border-none bg-transparent px-0.5 py-1 text-[13.5px] text-ink outline-none placeholder:text-placeholder"
            />
          </div>
        </Field>

        <Field label="문제 제목">
          <Input value={form.title} onChange={(e) => update({ title: e.target.value })} />
        </Field>

        <Field label="문제 본문">
          <textarea
            value={form.body}
            onChange={(e) => update({ body: e.target.value })}
            className={`${TEXTAREA_CLASS} min-h-[110px]`}
          />
        </Field>

        <Field label="해설">
          <textarea
            value={form.explanation}
            onChange={(e) => update({ explanation: e.target.value })}
            className={`${TEXTAREA_CLASS} min-h-[100px]`}
          />
        </Field>
      </Card>

      <Card className="flex flex-col gap-3.5 p-6">
        <div className="flex items-center justify-between gap-3">
          <span className="text-[13px] font-semibold text-muted">
            보기 · {form.options.length}개
          </span>
          <span className="text-[12.5px] font-medium text-placeholder">
            정답으로 지정된 보기 1개
          </span>
        </div>

        {form.options.map((option, i) => {
          const isAnswer = form.answerIndex === i;
          return (
            <div
              key={i}
              className="flex flex-col gap-2.5 rounded-[12px] border border-line-card p-4"
            >
              <div className="flex items-center gap-3.5">
                <span className="w-3 flex-shrink-0 font-mono text-[13px] font-semibold text-placeholder">
                  {i + 1}
                </span>
                <Input
                  value={option.text}
                  onChange={(e) => updateOption(i, { text: e.target.value })}
                  className="min-w-0 flex-1"
                />
                <button
                  type="button"
                  onClick={() => update({ answerIndex: i })}
                  className="flex flex-shrink-0 cursor-pointer items-center gap-[7px]"
                >
                  <span
                    className={`block h-4 w-4 rounded-full border-2 ${
                      isAnswer ? "border-success" : "border-line-strong"
                    }`}
                  />
                  <span
                    className={`text-[12.5px] font-semibold ${
                      isAnswer ? "text-success" : "text-placeholder"
                    }`}
                  >
                    정답
                  </span>
                </button>
              </div>

              <input
                value={option.explanation}
                onChange={(e) => updateOption(i, { explanation: e.target.value })}
                placeholder={
                  isAnswer
                    ? "이 보기를 고른 경우 오답 사유 해설 (정답 보기는 미입력)"
                    : "이 보기를 고른 경우 오답 사유 해설"
                }
                className={`w-full rounded-[10px] border-none px-3.5 py-2.5 text-[13.5px] outline-none ${
                  isAnswer
                    ? "bg-subtle text-placeholder placeholder:text-placeholder"
                    : "bg-alert-bg text-alert-deep placeholder:text-alert-deep/70"
                }`}
              />
            </div>
          );
        })}
      </Card>

      <div className="flex items-center justify-end gap-2.5">
        <Button variant="muted" size="md" onClick={onCancel}>
          취소
        </Button>
        <Button size="lg" onClick={() => setSaved(true)}>
          저장
        </Button>
      </div>

      {saved && (
        <div className="flex items-center gap-3 rounded-[16px] border border-success-pale bg-success-bg px-[22px] py-[18px]">
          <span className="flex h-[38px] w-[38px] flex-shrink-0 items-center justify-center rounded-full bg-success text-base font-bold text-white">
            ✓
          </span>
          <span className="flex flex-col gap-[3px]">
            <span className="text-sm font-bold text-success">저장되었습니다.</span>
            <span className="text-[12.5px] font-medium text-secondary">
              어드민 저장 API가 아직 없어 실제 반영은 되지 않습니다.
            </span>
          </span>
        </div>
      )}
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-[7px]">
      <span className="text-[12.5px] font-semibold text-placeholder">{label}</span>
      {children}
    </div>
  );
}
