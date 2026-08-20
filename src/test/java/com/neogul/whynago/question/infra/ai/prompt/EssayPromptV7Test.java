package com.neogul.whynago.question.infra.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.EssayGradingMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EssayPromptV7Test {

    private final EssayPromptV7 prompt = new EssayPromptV7();

    @Test
    void version() {
        assertThat(prompt.version()).isEqualTo("v7");
    }

    @ParameterizedTest
    @EnumSource(EssayGradingMode.class)
    void keepsV6RulesAndAddsFewShotExamples(EssayGradingMode mode) {
        String v6 = new EssayPromptV6().systemPrompt(mode);
        String v7 = prompt.systemPrompt(mode);

        assertThat(v7)
                .startsWith(v6)
                .contains("[판정 예시]")
                .contains("부분 정답과 루브릭")
                .contains("모름 답변")
                .contains("criteriaResults")
                .contains("NOT_LEARNED");
    }
}
