import type { AdminQuestionView } from "@/mocks/admin";
import Card, { CardHeader } from "@/components/ui/Card";
import StatCard from "@/components/ui/StatCard";

// 문제 상세 · 통계 섹션
export default function QuestionStats({ question }: { question: AdminQuestionView }) {
  const { detail } = question;

  return (
    <div className="flex flex-col gap-[18px]">
      <span className="text-[13px] font-bold text-muted">통계</span>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {question.statCards.map((card) => (
          <StatCard key={card.label} label={card.label}>
            <span className="font-mono text-[26px] font-bold tracking-[-0.5px] text-ink">
              {card.value}
              {card.unit && (
                <span className="ml-[3px] font-sans text-sm font-semibold text-placeholder">
                  {card.unit}
                </span>
              )}
            </span>
          </StatCard>
        ))}
      </div>

      {detail.distribution && (
        <Card className="overflow-hidden">
          <CardHeader className="justify-between gap-3">
            <span className="text-[13px] font-semibold text-secondary">보기별 선택 분포</span>
            <span className="flex-shrink-0 whitespace-nowrap text-[12.5px] font-medium text-placeholder">
              응답{" "}
              <span className="font-mono font-semibold text-secondary">
                {(detail.responseCount ?? 0).toLocaleString()}
              </span>
              건
            </span>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-[18px]">
            {detail.distribution.map((row) => (
              <div key={row.label} className="flex items-center gap-4">
                <span
                  className={`w-[60px] flex-shrink-0 text-[12.5px] font-semibold ${
                    row.correct ? "text-success" : "text-placeholder"
                  }`}
                >
                  {row.label}
                </span>
                <span
                  className={`w-[300px] min-w-[120px] truncate text-sm text-ink ${
                    row.correct ? "font-bold" : "font-medium"
                  }`}
                >
                  {row.text}
                </span>
                <div className="h-2 min-w-[60px] flex-1 overflow-hidden rounded-md bg-neutral">
                  {/* 폭이 데이터에서 오므로 유틸리티 클래스로 표현할 수 없다 */}
                  <div
                    style={{ width: row.rate }}
                    className={`h-full rounded-md ${row.correct ? "bg-success" : "bg-placeholder"}`}
                  />
                </div>
                <span className="w-14 flex-shrink-0 text-right font-mono text-[13.5px] font-medium text-secondary">
                  {row.count.toLocaleString()}
                </span>
                <span
                  className={`w-14 flex-shrink-0 text-right font-mono text-[13.5px] font-bold ${
                    row.correct ? "text-success" : "text-secondary"
                  }`}
                >
                  {row.rate}
                </span>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
