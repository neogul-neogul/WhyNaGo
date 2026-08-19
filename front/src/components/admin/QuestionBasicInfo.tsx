import type { AdminQuestionView } from "@/mocks/admin";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";
import Card, { CardHeader } from "@/components/ui/Card";
import { CategoryBadge, DifficultyBadge, TypeBadge } from "@/components/admin/AdminBadges";

// 문제 상세 · 기본 정보 탭
export default function QuestionBasicInfo({
  question,
  onEdit,
}: {
  question: AdminQuestionView;
  onEdit: () => void;
}) {
  const { detail } = question;

  return (
    <Card className="overflow-hidden">
      <CardHeader className="flex-wrap justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2.5">
          <span className="font-mono text-[13.5px] font-semibold text-secondary">{question.id}</span>
          <CategoryBadge category={question.category} />
          <DifficultyBadge difficulty={question.difficulty} />
          <TypeBadge type={question.type} />
        </div>
        <Button size="md" onClick={onEdit}>
          문제 수정
        </Button>
      </CardHeader>

      <div className="flex flex-col gap-[18px] p-[22px]">
        <Field label="문제 제목">
          <span className="text-[17px] font-bold tracking-[-0.2px] text-ink">{detail.title}</span>
        </Field>

        <Field label="문제 본문">
          <span className="text-pretty text-sm font-semibold leading-[1.7] text-ink">
            {detail.body}
          </span>
        </Field>

        {detail.options && (
          <Field label="보기 · 정답">
            <div className="flex flex-col gap-2.5">
              {detail.options.map((option, i) => (
                <div
                  key={option.text}
                  className={`flex items-center gap-3.5 rounded-[11px] border px-[18px] py-[15px] ${
                    option.correct
                      ? "border-success-pale bg-success-bg"
                      : "border-line-card bg-subtle"
                  }`}
                >
                  <span className="w-3 flex-shrink-0 font-mono text-[13px] font-semibold text-placeholder">
                    {i + 1}
                  </span>
                  <span
                    className={`min-w-0 flex-1 text-sm ${option.correct ? "font-bold" : "font-medium"} text-ink`}
                  >
                    {option.text}
                  </span>
                  {option.correct && (
                    <span className="whitespace-nowrap text-[12.5px] font-bold text-success">
                      ✓ 정답
                    </span>
                  )}
                </div>
              ))}
            </div>
          </Field>
        )}

        {detail.answerExplanation && (
          <Field label="정답 해설">
            <div className="text-pretty rounded-[11px] border border-line-card bg-subtle px-5 py-[18px] text-sm font-medium leading-[1.75] text-ink">
              {detail.answerExplanation}
            </div>
          </Field>
        )}

        {detail.wrongExplanations && detail.wrongExplanations.length > 0 && (
          <Field label="보기별 오답 사유 해설">
            <div className="flex flex-col gap-2.5">
              {detail.wrongExplanations.map((w) => (
                <div
                  key={w.number}
                  className="flex items-start gap-3.5 rounded-[11px] border border-line-card bg-subtle px-[18px] py-[15px]"
                >
                  <span className="w-3 flex-shrink-0 font-mono text-[13px] font-semibold text-placeholder">
                    {w.number}
                  </span>
                  <span className="min-w-0 flex-1 text-sm font-medium leading-[1.7] text-secondary">
                    {w.text}
                  </span>
                </div>
              ))}
            </div>
          </Field>
        )}

        <div className="flex flex-wrap items-center gap-3.5 border-t border-dashed border-line pt-4">
          <div className="flex flex-wrap gap-1.5">
            {detail.tags.map((tag) => (
              <Badge key={tag} tone="neutral" size="xs">
                {tag}
              </Badge>
            ))}
          </div>
          <span className="font-mono text-[12.5px] font-medium text-placeholder">
            등록일 {detail.createdAt}
          </span>
          <span className="font-mono text-[12.5px] font-medium text-placeholder">
            최근 수정일 {detail.updatedAt} · {detail.updatedBy}
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
