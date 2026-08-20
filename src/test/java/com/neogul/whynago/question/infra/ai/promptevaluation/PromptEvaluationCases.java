package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

final class PromptEvaluationCases {

    private static final String RESOURCE = "/prompt-evaluation/cases.json";

    private PromptEvaluationCases() {
    }

    static List<PromptEvaluationCase> load(ObjectMapper objectMapper) {
        try (InputStream input = PromptEvaluationCases.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("프롬프트 평가 골든셋을 찾을 수 없습니다: " + RESOURCE);
            }
            List<PromptEvaluationCase> cases = objectMapper.readValue(input, new TypeReference<>() {
            });
            if (cases.isEmpty()) {
                throw new IllegalStateException("프롬프트 평가 골든셋은 비어 있을 수 없습니다.");
            }
            return cases;
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 평가 골든셋을 읽지 못했습니다.", e);
        }
    }
}
