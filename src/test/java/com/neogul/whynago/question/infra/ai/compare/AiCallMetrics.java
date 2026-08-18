package com.neogul.whynago.question.infra.ai.compare;

public record AiCallMetrics(String servedModel, long promptTokens, long completionTokens, long totalTokens) {
}
