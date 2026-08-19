import type { AdminQuestionDetailResponse } from "@/types";
import { TYPE_LABELS } from "@/lib/questions";
import { mockQuestionMeta } from "@/mocks/admin";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card, { CardHeader } from "@/components/ui/Card";
import { CategoryBadge, DifficultyBadge, TypeBadge } from "@/components/admin/AdminBadges";

// 문제 상세 · 기본 정보
export default function QuestionBasicInfo({
  question,
  onEdit,
}: {
  question: AdminQuestionDetailResponse;
  onEdit: () => void;
}) {
  // 오답 해설이 달린 보기만 모은다 (정답 보기는 해설이 비어 있다)
  const wrongExplanations = question.choices.filter((c) => !c.correct && c.explanation);
  // 등록일·수정일·수정자는 Question에 컬럼이 없어 API가 내려주지 않는다. 목업 값이다.
  const meta = mockQuestionMeta(question.id);

  return (
    <Card className="overflow-hidden">
      <CardHeader className="flex-wrap justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2.5">
          <span className="font-mono text-[13.5px] font-semibold text-secondary">{question.id}</span>
          <CategoryBadge category={question.category} />
          <DifficultyBadge difficulty={question.difficulty} />
          <TypeBadge type={TYPE_LABELS[question.type]} />
        </div>
        <Button size="md" onClick={onEdit}>
          문제 수정
        </Button>
      </CardHeader>

      <div className="flex flex-col gap-[18px] p-[22px]">
        <Field label="문제 제목">
          <span className="text-[17px] font-bold tracking-[-0.2px] text-ink">{question.title}</span>
        </Field>

        <Field label="문제 본문">
          <span className="text-pretty text-sm font-semibold leading-[1.7] text-ink">
            {question.content}
          </span>
        </Field>

        {question.choices.length > 0 && (
          <Field label="보기 · 정답">
            <div className="flex flex-col gap-2.5">
              {question.choices.map((choice) => (
                <div
                  key={choice.id}
                  className={`flex items-center gap-3.5 rounded-[11px] border px-[18px] py-[15px] ${
                    choice.correct
                      ? "border-success-pale bg-success-bg"
                      : "border-line-card bg-subtle"
                  }`}
                >
                  <span className="w-3 flex-shrink-0 font-mono text-[13px] font-semibold text-placeholder">
                    {choice.sequence}
                  </span>
                  <span
                    className={`min-w-0 flex-1 text-sm ${choice.correct ? "font-bold" : "font-medium"} text-ink`}
                  >
                    {choice.content}
                  </span>
                  {choice.correct && (
                    <span className="whitespace-nowrap text-[12.5px] font-bold text-success">
                      ✓ 정답
                    </span>
                  )}
                </div>
              ))}
            </div>
          </Field>
        )}

        {question.explanation && (
          <Field label="정답 해설">
            <div className="text-pretty rounded-[11px] border border-line-card bg-subtle px-5 py-[18px] text-sm font-medium leading-[1.75] text-ink">
              {question.explanation}
            </div>
          </Field>
        )}

        {wrongExplanations.length > 0 && (
          <Field label="보기별 오답 사유 해설">
            <div className="flex flex-col gap-2.5">
              {wrongExplanations.map((choice) => (
                <div
                  key={choice.id}
                  className="flex items-start gap-3.5 rounded-[11px] border border-line-card bg-subtle px-[18px] py-[15px]"
                >
                  <span className="w-3 flex-shrink-0 font-mono text-[13px] font-semibold text-placeholder">
                    {choice.sequence}
                  </span>
                  <span className="min-w-0 flex-1 text-sm font-medium leading-[1.7] text-secondary">
                    {choice.explanation}
                  </span>
                </div>
              ))}
            </div>
          </Field>
        )}

        <div className="flex flex-wrap items-center gap-3.5 border-t border-dashed border-line pt-4">
          <div className="flex flex-wrap gap-1.5">
            {question.tags.map((tag) => (
              <Badge key={tag} tone="neutral" size="xs">
                {tag}
              </Badge>
            ))}
          </div>
          <span className="font-mono text-[12.5px] font-medium text-placeholder">
            등록일 {meta.createdAt}
          </span>
          <span className="font-mono text-[12.5px] font-medium text-placeholder">
            최근 수정일 {meta.updatedAt} · {meta.updatedBy}
          </span>
        </div>
      </div>
    </Card>
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
