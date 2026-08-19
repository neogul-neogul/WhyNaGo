import type { AdminQuestionDetailResponse, MultipleChoiceStatisticsResponse } from "@/types";
import { formatElapsedSeconds, formatRate } from "@/lib/admin";
import Card, { CardHeader } from "@/components/ui/Card";
import StatCard from "@/components/ui/StatCard";

// 문제 상세 · 통계 섹션
// 서술형은 보기가 없어 최다 선택지·선택 분포가, 소요 시간을 수집하지 않아 평균 소요 시간이 성립하지 않는다.
// 그래서 객관식만 통계 API(statistics)를 받고, 서술형은 상세 응답의 풀이수·정답률만 쓴다.
export default function QuestionStats({
  question,
  statistics,
}: {
  question: AdminQuestionDetailResponse;
  statistics: MultipleChoiceStatisticsResponse | null;
}) {
  const mostChosen = statistics?.mostChosenChoice ?? null;

  const statCards = statistics
    ? [
        { label: "전체 풀이 횟수", value: statistics.totalSolveCount.toLocaleString(), unit: "회" },
        { label: "정답률", value: formatRate(statistics.correctRate), unit: "" },
        {
          label: "평균 소요 시간",
          value: formatElapsedSeconds(statistics.averageElapsedSeconds),
          unit: "",
        },
        {
          label: "가장 많이 고른 선택지",
          value: mostChosen ? `${mostChosen.sequence}번` : "-",
          unit: mostChosen ? formatRate(mostChosen.selectedRate) : "",
        },
      ]
    : [
        { label: "전체 풀이 횟수", value: (question.solveCount ?? 0).toLocaleString(), unit: "회" },
        { label: "정답률", value: formatRate(question.correctRate), unit: "" },
      ];

  return (
    <div className="flex flex-col gap-[18px]">
      <span className="text-[13px] font-bold text-muted">통계</span>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {statCards.map((card) => (
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

      {statistics && (
        <Card className="overflow-hidden">
          <CardHeader className="justify-between gap-3">
            <span className="text-[13px] font-semibold text-secondary">보기별 선택 분포</span>
            <span className="flex-shrink-0 whitespace-nowrap text-[12.5px] font-medium text-placeholder">
              응답{" "}
              <span className="font-mono font-semibold text-secondary">
                {statistics.totalSolveCount.toLocaleString()}
              </span>
              건
            </span>
          </CardHeader>
          <div className="flex flex-col gap-3.5 px-[22px] py-[18px]">
            {statistics.choiceDistribution.map((row) => (
              <div key={row.choiceId} className="flex items-center gap-4">
                <span
                  className={`w-[60px] flex-shrink-0 text-[12.5px] font-semibold ${
                    row.correct ? "text-success" : "text-placeholder"
                  }`}
                >
                  {row.correct ? "✓ 정답" : `보기 ${row.sequence}`}
                </span>
                <span
                  className={`w-[300px] min-w-[120px] truncate text-sm text-ink ${
                    row.correct ? "font-bold" : "font-medium"
                  }`}
                >
                  {row.content}
                </span>
                <div className="h-2 min-w-[60px] flex-1 overflow-hidden rounded-md bg-neutral">
                  {/* 폭이 데이터에서 오므로 유틸리티 클래스로 표현할 수 없다 */}
                  <div
                    style={{ width: `${row.selectedRate}%` }}
                    className={`h-full rounded-md ${row.correct ? "bg-success" : "bg-placeholder"}`}
                  />
                </div>
                <span className="w-14 flex-shrink-0 text-right font-mono text-[13.5px] font-medium text-secondary">
                  {row.selectedCount.toLocaleString()}
                </span>
                <span
                  className={`w-14 flex-shrink-0 text-right font-mono text-[13.5px] font-bold ${
                    row.correct ? "text-success" : "text-secondary"
                  }`}
                >
                  {formatRate(row.selectedRate)}
                </span>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
