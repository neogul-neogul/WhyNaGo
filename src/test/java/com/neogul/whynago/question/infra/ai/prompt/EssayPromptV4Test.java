package com.neogul.whynago.question.infra.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.fixture.EssayGradingTargetFixture;
import com.neogul.whynago.question.domain.EssayGradingMode;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EssayPromptV4Test {

    private final EssayPromptV4 prompt = new EssayPromptV4();

    @ParameterizedTest
    @EnumSource(EssayGradingMode.class)
    @DisplayName("두 채점 모드 모두 숙련도 6분류의 판정 기준을 프롬프트에 담는다.")
    void systemPrompt_containsAllMasteryLevels(EssayGradingMode mode) {
        String systemPrompt = prompt.systemPrompt(mode);

        assertThat(Arrays.stream(MasteryLevel.values()).map(Enum::name))
                .allSatisfy(level -> assertThat(systemPrompt).contains(level));
    }

    @ParameterizedTest
    @EnumSource(EssayGradingMode.class)
    @DisplayName("두 채점 모드 모두 판정 근거를 반드시 채우도록 지시한다.")
    void systemPrompt_requiresMasteryReason(EssayGradingMode mode) {
        String systemPrompt = prompt.systemPrompt(mode);

        assertThat(systemPrompt).contains("masteryReason");
        // 일반론으로 근거를 때우는 응답을 막는 지시가 빠지면 근거가 쓸모없어진다.
        assertThat(systemPrompt).contains("일반론은 금지");
        assertThat(systemPrompt).contains("score와 mastery는 서로 모순되지 않아야 한다");
    }

    @Test
    @DisplayName("v3의 채점 규칙을 그대로 승계한다.")
    void systemPrompt_keepsPreviousRules() {
        String v3 = new EssayPromptV3().systemPrompt(EssayGradingMode.PRACTICE);
        String v4 = prompt.systemPrompt(EssayGradingMode.PRACTICE);

        // v3 본문을 잃어버리면 피드백 톤 규칙이 통째로 사라진다.
        assertThat(v4).contains("한국어로 작성하고, 정답을 단정하기보다 보완할 점 중심으로");
        assertThat(v4.length()).isGreaterThan(v3.length());
    }

    @Test
    @DisplayName("버전 문자열로 어떤 프롬프트가 쓰였는지 로그에서 구분할 수 있다.")
    void version() {
        assertThat(prompt.version()).isEqualTo("v4");
    }

    @Test
    @DisplayName("마지막 턴에는 꼬리질문을 생성하지 말라고 지시한다.")
    void userPrompt_lastTurn() {
        String userPrompt = prompt.userPrompt(EssayGradingMode.PRACTICE, EssayGradingTargetFixture.plain(), false);

        assertThat(userPrompt).contains("꼬리질문을 생성하지 말고");
        assertThat(userPrompt).contains("질문: 질문").contains("답변: 답변");
    }
}
