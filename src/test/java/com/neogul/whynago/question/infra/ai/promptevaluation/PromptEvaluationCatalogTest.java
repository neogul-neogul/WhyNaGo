package com.neogul.whynago.question.infra.ai.promptevaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PromptEvaluationCatalogTest {

    private static final String DIFFERENCE = "후보는 꼬리질문을 채점 결과에 맞춘다.";

    @Test
    @DisplayName("기본 비교는 등록되어 있고 검증을 통과한다.")
    void comparison_invalidDefault() {
        PromptComparison comparison = PromptEvaluationCatalog.comparison(PromptEvaluationCatalog.DEFAULT_COMPARISON);

        assertThatCode(() -> PromptEvaluationCatalog.validate(comparison)).doesNotThrowAnyException();
        assertThat(comparison.prompts()).hasSize(2);
        assertThat(comparison.difference()).isNotBlank();
    }

    @Test
    @DisplayName("운영 버전과의 비교도 이름으로 골라 쓸 수 있다.")
    void comparison_missingProductionPair() {
        PromptComparison comparison = PromptEvaluationCatalog.comparison("v6-v8");

        assertThatCode(() -> PromptEvaluationCatalog.validate(comparison)).doesNotThrowAnyException();
        assertThat(comparison.baselineVersion()).isEqualTo("v6");
        assertThat(comparison.candidateVersion()).isEqualTo("v8");
    }

    @Test
    @DisplayName("등록하지 않은 이름을 고르면 등록된 비교 목록과 함께 실패한다.")
    void comparison_unknownName() {
        assertThatThrownBy(() -> PromptEvaluationCatalog.comparison("v6-v7"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("등록되지 않은 비교")
                .hasMessageContaining("v7-v8");
    }

    @Test
    @DisplayName("v6 이전 프롬프트는 응답 형식이 달라 비교할 수 없다.")
    void validate_promptBeforeV6() {
        assertThatThrownBy(() -> PromptEvaluationCatalog.validate(
                new PromptComparison(prompt("v5"), prompt("v8"), DIFFERENCE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("v6 이후");
    }

    @Test
    @DisplayName("같은 버전끼리는 비교할 수 없다.")
    void validate_sameVersion() {
        assertThatThrownBy(() -> PromptEvaluationCatalog.validate(
                new PromptComparison(prompt("v7"), prompt("v7"), DIFFERENCE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("서로 다른 두 프롬프트");
    }

    @Test
    @DisplayName("후보가 기준선보다 이전 버전이면 거부한다.")
    void validate_candidateOlderThanBaseline() {
        assertThatThrownBy(() -> PromptEvaluationCatalog.validate(
                new PromptComparison(prompt("v8"), prompt("v7"), DIFFERENCE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("나중 버전");
    }

    @Test
    @DisplayName("핵심 차이를 비워 두면 심사 기준이 없으므로 거부한다.")
    void validate_blankDifference() {
        assertThatThrownBy(() -> PromptEvaluationCatalog.validate(
                new PromptComparison(prompt("v7"), prompt("v8"), "  ")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("핵심 차이");
    }

    private EssayPrompt prompt(String version) {
        return new EssayPrompt() {
            @Override
            public String version() {
                return version;
            }

            @Override
            public String systemPrompt(EssayGradingMode mode) {
                return "system";
            }

            @Override
            public String userPrompt(EssayGradingMode mode, EssayGradingTarget target, boolean generateFollowup) {
                return "user";
            }
        };
    }
}
