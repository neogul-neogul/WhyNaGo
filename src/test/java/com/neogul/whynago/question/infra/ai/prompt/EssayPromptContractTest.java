package com.neogul.whynago.question.infra.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.EssayGradingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EssayPromptContractTest {

    private static final List<EssayPrompt> PROMPTS = List.of(
            new EssayPromptV1(),
            new EssayPromptV2(),
            new EssayPromptV3());

    private static final String QUESTION = "고유질문-a1b2c3";
    private static final String ANSWER = "고유답변-x9y8z7";

    private static Stream<Named<EssayPrompt>> prompts() {
        return PROMPTS.stream().map(prompt -> Named.of(prompt.version(), prompt));
    }

    private static Stream<Arguments> promptsAndModes() {
        return PROMPTS.stream()
                .flatMap(prompt -> Arrays.stream(EssayGradingMode.values())
                        .map(mode -> Arguments.of(Named.of(prompt.version(), prompt), mode)));
    }

    @Test
    @DisplayName("프롬프트 버전 값은 구현체마다 서로 다르다.")
    void version_duplicated() {
        List<String> versions = PROMPTS.stream().map(EssayPrompt::version).toList();

        assertThat(versions).doesNotHaveDuplicates();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prompts")
    @DisplayName("모든 프롬프트 버전은 버전 값을 가진다.")
    void version_blank(EssayPrompt prompt) {
        assertThat(prompt.version()).isNotBlank();
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("promptsAndModes")
    @DisplayName("모든 채점 모드에 시스템 프롬프트를 제공한다.")
    void systemPrompt_blank(EssayPrompt prompt, EssayGradingMode mode) {
        assertThat(prompt.systemPrompt(mode)).isNotBlank();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prompts")
    @DisplayName("시스템 프롬프트는 채점 모드에 따라 달라진다.")
    void systemPrompt_sameForEveryMode(EssayPrompt prompt) {
        assertThat(prompt.systemPrompt(EssayGradingMode.INTERVIEW))
                .isNotEqualTo(prompt.systemPrompt(EssayGradingMode.PRACTICE));
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("promptsAndModes")
    @DisplayName("사용자 프롬프트는 채점 대상 질문과 답변을 그대로 담는다.")
    void userPrompt_missingGradingTarget(EssayPrompt prompt, EssayGradingMode mode) {
        assertThat(prompt.userPrompt(mode, QUESTION, ANSWER, true))
                .contains(QUESTION)
                .contains(ANSWER);
        assertThat(prompt.userPrompt(mode, QUESTION, ANSWER, false))
                .contains(QUESTION)
                .contains(ANSWER);
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("promptsAndModes")
    @DisplayName("사용자 프롬프트에 채우지 못한 서식 문자를 남기지 않는다.")
    void userPrompt_unfilledPlaceholder(EssayPrompt prompt, EssayGradingMode mode) {
        assertThat(prompt.userPrompt(mode, QUESTION, ANSWER, true)).doesNotContain("%s");
        assertThat(prompt.userPrompt(mode, QUESTION, ANSWER, false)).doesNotContain("%s");
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("promptsAndModes")
    @DisplayName("꼬리질문을 만드는 턴과 만들지 않는 턴의 지시가 서로 다르다.")
    void userPrompt_sameForFollowupTurnAndNot(EssayPrompt prompt, EssayGradingMode mode) {
        assertThat(prompt.userPrompt(mode, QUESTION, ANSWER, true))
                .isNotEqualTo(prompt.userPrompt(mode, QUESTION, ANSWER, false));
    }
}
