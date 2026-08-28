package com.neogul.whynago.question.infra.ai.compare;

import java.util.List;

/**
 * 한 모델이 한 시나리오를 한 번 수행한 결과.
 * 호출이 실패해도 비교를 계속할 수 있도록 실패 사유를 결과에 담아 전달한다.
 */
public record ModelScenarioResult(
        String model,
        int attempt,
        EssayComparisonScenario scenario,
        List<EssayTurnResponse> turns,
        String failure
) {

    public static ModelScenarioResult completed(
            String model,
            int attempt,
            EssayComparisonScenario scenario,
            List<EssayTurnResponse> turns
    ) {
        return new ModelScenarioResult(model, attempt, scenario, List.copyOf(turns), null);
    }

    public static ModelScenarioResult failed(
            String model,
            int attempt,
            EssayComparisonScenario scenario,
            List<EssayTurnResponse> completedTurns,
            Throwable cause
    ) {
        return new ModelScenarioResult(model, attempt, scenario, List.copyOf(completedTurns), cause.toString());
    }

    public boolean succeeded() {
        return failure == null;
    }

    public long totalElapsedMs() {
        return turns.stream().mapToLong(EssayTurnResponse::elapsedMs).sum();
    }

    public long totalTokens() {
        return turns.stream()
                .map(EssayTurnResponse::metrics)
                .filter(metrics -> metrics != null)
                .mapToLong(AiCallMetrics::totalTokens)
                .sum();
    }
}
