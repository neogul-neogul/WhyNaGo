package com.neogul.whynago.question.infra.ai.compare;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 모델별 응답을 사람이 눈으로 비교할 수 있게 마크다운으로 모아 쓴다.
 * 점수·소요 시간·토큰은 표로, 실제 문장(feedback·modelAnswer·followupQuestion)은 본문으로 남긴다.
 */
public class EssayComparisonReporter {

    private static final Path REPORT_PATH =
            Path.of("build", "reports", "ai-model-comparison", "essay-model-comparison.md");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String NOT_MEASURED = "-";

    private final EssayAiComparisonConfig config;
    private final Map<String, List<ModelScenarioResult>> resultsByScenario = new LinkedHashMap<>();

    public EssayComparisonReporter(EssayAiComparisonConfig config) {
        this.config = config;
    }

    public void add(ModelScenarioResult result) {
        resultsByScenario
                .computeIfAbsent(result.scenario().name(), name -> new ArrayList<>())
                .add(result);
    }

    public boolean isEmpty() {
        return resultsByScenario.isEmpty();
    }

    public Path write() {
        String markdown = markdown();
        try {
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(REPORT_PATH, markdown, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.out.println(markdown);
        return REPORT_PATH.toAbsolutePath();
    }

    private String markdown() {
        StringBuilder report = new StringBuilder();
        appendHeader(report);
        resultsByScenario.forEach((scenarioName, results) -> appendScenario(report, results));
        return report.toString();
    }

    private void appendHeader(StringBuilder report) {
        report.append("# AI 모델 응답 비교\n\n")
                .append("- 실행 시각: ").append(LocalDateTime.now().format(TIMESTAMP)).append('\n')
                .append("- 설정: ").append(config.displayName())
                .append(" (").append(config.baseUrl()).append(")\n")
                .append("- 비교 모델: ").append(String.join(", ", config.models())).append('\n')
                .append("- 프롬프트 버전: ").append(config.promptVersion()).append('\n')
                .append("- temperature: ").append(config.temperature())
                .append(", reasoningEffort: ")
                .append(config.hasReasoningEffort() ? config.reasoningEffort() : NOT_MEASURED)
                .append("\n\n");
    }

    private void appendScenario(StringBuilder report, List<ModelScenarioResult> results) {
        EssayComparisonScenario scenario = results.getFirst().scenario();

        report.append("## 시나리오: ").append(scenario.name())
                .append(" (").append(scenario.mode()).append(", ")
                .append(scenario.turnCount()).append("턴)\n\n")
                .append("- 질문: ").append(oneLine(scenario.question())).append('\n')
                .append("- 답변: ").append(oneLine(scenario.answers().getFirst())).append("\n\n");

        appendSummaryTable(report, results);
        appendScoreSpread(report, results);
        results.forEach(result -> appendDetail(report, result));
    }

    private void appendSummaryTable(StringBuilder report, List<ModelScenarioResult> results) {
        report.append("| 모델 | 시도 | 턴별 점수 | 총 소요(ms) | 총 토큰 | 응답 모델 | 비고 |\n")
                .append("| --- | --- | --- | --- | --- | --- | --- |\n");
        for (ModelScenarioResult result : results) {
            report.append("| ").append(result.model())
                    .append(" | ").append(result.attempt())
                    .append(" | ").append(scores(result))
                    .append(" | ").append(result.totalElapsedMs())
                    .append(" | ").append(result.totalTokens())
                    .append(" | ").append(servedModel(result))
                    .append(" | ").append(result.succeeded() ? "" : "실패: " + oneLine(result.failure()))
                    .append(" |\n");
        }
        report.append('\n');
    }

    // 같은 모델을 여러 번 호출한 시나리오에서만 점수 흔들림을 요약한다.
    private void appendScoreSpread(StringBuilder report, List<ModelScenarioResult> results) {
        Map<String, List<Integer>> firstTurnScoresByModel = new LinkedHashMap<>();
        for (ModelScenarioResult result : results) {
            if (!result.turns().isEmpty()) {
                firstTurnScoresByModel
                        .computeIfAbsent(result.model(), model -> new ArrayList<>())
                        .add(result.turns().getFirst().result().score());
            }
        }
        if (firstTurnScoresByModel.values().stream().noneMatch(scores -> scores.size() > 1)) {
            return;
        }

        report.append("첫 턴 점수 편차\n\n");
        firstTurnScoresByModel.forEach((model, scores) -> {
            int min = scores.stream().mapToInt(Integer::intValue).min().orElse(0);
            int max = scores.stream().mapToInt(Integer::intValue).max().orElse(0);
            report.append("- ").append(model).append(": ").append(scores)
                    .append(" (최소 ").append(min).append(", 최대 ").append(max)
                    .append(", 폭 ").append(max - min).append(")\n");
        });
        report.append('\n');
    }

    private void appendDetail(StringBuilder report, ModelScenarioResult result) {
        report.append("### ").append(result.model())
                .append(" (시도 ").append(result.attempt()).append(")\n\n");

        if (!result.succeeded()) {
            report.append("> 호출 실패: ").append(result.failure()).append("\n\n");
        }

        for (EssayTurnResponse turn : result.turns()) {
            report.append("**턴 ").append(turn.turn())
                    .append("** (score ").append(turn.result().score())
                    .append(", ").append(turn.elapsedMs()).append("ms")
                    .append(", 토큰 ").append(tokens(turn)).append(")\n\n")
                    .append("- 질문: ").append(oneLine(turn.question())).append('\n')
                    .append("- 답변: ").append(oneLine(turn.answer())).append('\n')
                    .append("- feedback: ").append(oneLine(turn.result().feedback())).append('\n')
                    .append("- modelAnswer: ").append(oneLine(turn.result().modelAnswer())).append('\n')
                    .append("- followupQuestion")
                    .append(turn.followupRequested() ? "" : " (요청 안 함)")
                    .append(": ").append(oneLine(turn.result().followupQuestion())).append("\n\n");
        }
    }

    private String scores(ModelScenarioResult result) {
        if (result.turns().isEmpty()) {
            return NOT_MEASURED;
        }
        return result.turns().stream()
                .map(turn -> String.valueOf(turn.result().score()))
                .reduce((first, second) -> first + " → " + second)
                .orElse(NOT_MEASURED);
    }

    private String servedModel(ModelScenarioResult result) {
        return result.turns().stream()
                .map(EssayTurnResponse::metrics)
                .filter(metrics -> metrics != null)
                .map(AiCallMetrics::servedModel)
                .findFirst()
                .orElse(NOT_MEASURED);
    }

    private String tokens(EssayTurnResponse turn) {
        AiCallMetrics metrics = turn.metrics();
        if (metrics == null) {
            return NOT_MEASURED;
        }
        return "%d/%d".formatted(metrics.promptTokens(), metrics.completionTokens());
    }

    // 표와 목록이 깨지지 않게 줄바꿈과 파이프를 정리한다.
    private String oneLine(String text) {
        if (text == null) {
            return "(없음)";
        }
        return text.replaceAll("\\s+", " ").replace("|", "\\|").trim();
    }
}
