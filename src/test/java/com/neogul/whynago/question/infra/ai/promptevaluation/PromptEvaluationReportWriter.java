package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class PromptEvaluationReportWriter {

    private static final DateTimeFormatter WRITTEN_AT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm z");

    private final Clock clock;
    private final PromptComparison comparison;
    private final ObjectMapper objectMapper = new ObjectMapper();

    PromptEvaluationReportWriter(Clock clock, PromptComparison comparison) {
        this.clock = clock;
        this.comparison = comparison;
    }

    void write(Path path, List<PromptEvaluationRun> runs) {
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Files.writeString(path, render(runs));
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 평가 Markdown 리포트를 저장하지 못했습니다: " + path, e);
        }
    }

    String render(List<PromptEvaluationRun> runs) {
        String baselineVersion = comparison.baselineVersion();
        Map<String, Integer> baselineScores = runs.stream()
                .filter(run -> run.prompt().version().equals(baselineVersion))
                .collect(Collectors.toMap(PromptEvaluationRun::caseId, run -> run.judge().total()));

        StringBuilder markdown = new StringBuilder();
        // 차이 설명을 맨 위에 둔다. 이 리포트를 나중에 다시 열었을 때 점수보다 먼저 읽어야 하는 것이
        // "무엇을 바꿨는가"이고, 심사 점수도 이 차이를 기준으로 매겨졌기 때문이다.
        markdown.append("# 서술형 프롬프트 평가\n\n")
                .append("## 비교 대상: `").append(baselineVersion).append("`(기준선) vs `")
                .append(comparison.candidateVersion()).append("`(후보)\n\n")
                .append("### 두 프롬프트의 핵심 차이\n\n")
                .append(comparison.difference().strip()).append("\n\n")
                .append("심사 모델에도 위 차이를 그대로 주고, 그 차이가 응답에 실제로 나타났는지를 기준으로 채점하게 했다.\n")
                .append("어느 버전의 응답인지는 심사 모델에 알려 주지 않는다.\n\n")
                .append("- 실행 시각: ").append(WRITTEN_AT.format(ZonedDateTime.now(clock))).append("\n")
                .append("- 생성/심사 모델: 로컬 Ollama (`OLLAMA_MODEL`)\n")
                .append("- 심사 프롬프트: `").append(PromptEvaluationJudge.VERSION).append("`\n\n")
                .append("## 요약\n\n")
                .append("| 케이스 | 프롬프트 | 채점 | 루브릭 | 피드백 | 모범답안 | 관련성 | 난이도 | 가드레일 | 차이 반영 | 총점 | ")
                .append(baselineVersion).append(" 대비 | 계약 | 상세 |\n")
                .append("| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |\n");

        for (PromptEvaluationRun run : runs) {
            PromptEvaluationJudge.JudgeResult score = run.judge();
            int delta = score.total() - baselineScores.getOrDefault(run.caseId(), score.total());
            markdown.append("| ").append(escapeCell(run.caseId()))
                    .append(" | `").append(run.prompt().version()).append("`")
                    .append(" | ").append(score.gradingAccuracy())
                    .append(" | ").append(score.rubricScoreConsistency())
                    .append(" | ").append(score.feedbackQuality())
                    .append(" | ").append(score.modelAnswerQuality())
                    .append(" | ").append(score.followupRelevance())
                    .append(" | ").append(score.followupDifficulty())
                    .append(" | ").append(score.followupGuardrails())
                    .append(" | ").append(score.differenceImpact())
                    .append(" | ").append(score.total())
                    .append(" | ").append(delta >= 0 ? "+" + delta : delta)
                    .append(" | ").append(run.contractPassed() ? "통과" : escapeCell(String.join(", ", run.contractFailures())))
                    .append(" | [보기](#").append(caseAnchor(run.caseId())).append(") |\n");
        }

        markdown.append("\n## 상세\n");
        runs.stream()
                .collect(Collectors.groupingBy(PromptEvaluationRun::caseId))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> appendCaseComparison(markdown, entry.getKey(), entry.getValue().stream()
                        .sorted(Comparator.comparingInt(run ->
                                run.prompt().version().equals(comparison.baselineVersion()) ? 0 : 1))
                        .toList()));
        return markdown.toString();
    }

    private void appendCaseComparison(StringBuilder markdown, String caseId, List<PromptEvaluationRun> runs) {
        PromptEvaluationCase testCase = runs.getFirst().testCase();
        markdown.append("\n### ").append(caseId).append("\n\n")
                .append("#### 평가 입력\n\n")
                .append("| 항목 | 값 |\n")
                .append("| --- | --- |\n")
                .append("| 모드 | ").append(testCase.mode()).append(" |\n")
                .append("| 꼬리질문 생성 | ").append(testCase.generateFollowup() ? "예" : "아니오").append(" |\n")
                .append("| 질문 | ").append(escapeCell(testCase.question())).append(" |\n")
                .append("| 사용자 답변 | ").append(escapeCell(testCase.answer())).append(" |\n")
                .append("| 루브릭 | ").append(escapeCell(String.valueOf(testCase.rubric()))).append(" |\n")
                .append("| 꼬리질문 허용 범위 | ").append(escapeCell(String.valueOf(testCase.followupScope()))).append(" |\n")
                .append("| 기대 기준 | ").append(escapeCell(String.valueOf(testCase.expected()))).append(" |\n\n");
        markdown.append("\n| 응답 필드 |");
        runs.forEach(run -> markdown.append(" `").append(run.prompt().version()).append("` |"));
        markdown.append("\n| --- |");
        runs.forEach(ignored -> markdown.append(" --- |"));
        markdown.append("\n");

        appendComparisonRow(markdown, "feedback", runs, run -> run.response().feedback());
        appendComparisonRow(markdown, "modelAnswer", runs, run -> run.response().modelAnswer());
        appendComparisonRow(markdown, "score", runs, run -> String.valueOf(run.response().score()));
        appendComparisonRow(markdown, "mastery", runs, run -> String.valueOf(run.response().mastery()));
        appendComparisonRow(markdown, "masteryReason", runs, run -> run.response().masteryReason());
        appendComparisonRow(markdown, "followupQuestion", runs, run -> run.response().followupQuestion());
        appendComparisonRow(markdown, "criteriaResults", runs, run -> prettyCriteriaResults(run));
        appendComparisonRow(markdown, "계약 검사", runs,
                run -> run.contractPassed() ? "통과" : String.join(", ", run.contractFailures()));

        markdown.append("\n#### 심사 결과\n\n| 심사 항목 |");
        runs.forEach(run -> markdown.append(" `").append(run.prompt().version()).append("` |"));
        markdown.append("\n| --- |");
        runs.forEach(ignored -> markdown.append(" --- |"));
        markdown.append("\n");
        appendComparisonRow(markdown, "심사 근거", runs, run -> run.judge().rationale());
        appendComparisonRow(markdown, "채점 정확성", runs, run -> String.valueOf(run.judge().gradingAccuracy()));
        appendComparisonRow(markdown, "루브릭/점수 일관성", runs,
                run -> String.valueOf(run.judge().rubricScoreConsistency()));
        appendComparisonRow(markdown, "피드백", runs, run -> String.valueOf(run.judge().feedbackQuality()));
        appendComparisonRow(markdown, "모범답안", runs, run -> String.valueOf(run.judge().modelAnswerQuality()));
        appendComparisonRow(markdown, "꼬리질문 관련성", runs, run -> String.valueOf(run.judge().followupRelevance()));
        appendComparisonRow(markdown, "꼬리질문 난이도", runs, run -> String.valueOf(run.judge().followupDifficulty()));
        appendComparisonRow(markdown, "꼬리질문 가드레일", runs,
                run -> String.valueOf(run.judge().followupGuardrails()));
        appendComparisonRow(markdown, "차이 반영", runs, run -> String.valueOf(run.judge().differenceImpact()));
        appendComparisonRow(markdown, "심사 총점", runs, run -> String.valueOf(run.judge().total()));
    }

    private String prettyCriteriaResults(PromptEvaluationRun run) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(run.response().criteriaResults());
            return "<pre><code class=\"language-json\">" + escapeHtml(json).replace("\n", "<br>") + "</code></pre>";
        } catch (JsonProcessingException e) {
            return "JSON 변환 실패: " + run.response().criteriaResults();
        }
    }

    private void appendComparisonRow(
            StringBuilder markdown,
            String field,
            List<PromptEvaluationRun> runs,
            java.util.function.Function<PromptEvaluationRun, String> valueOf
    ) {
        markdown.append("| ").append(escapeCell(field)).append(" |");
        runs.forEach(run -> markdown.append(" ").append(escapeCell(valueOf.apply(run))).append(" |"));
        markdown.append("\n");
    }

    private String caseAnchor(String caseId) {
        return caseId.toLowerCase().replaceAll("[^a-z0-9-]", "");
    }

    private String escapeCell(String value) {
        return escapeText(value).replace("|", "\\|");
    }

    private String escapeText(String value) {
        return value == null ? "(없음)" : value.replace("\r\n", "\n").replace("\n", "<br>");
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
