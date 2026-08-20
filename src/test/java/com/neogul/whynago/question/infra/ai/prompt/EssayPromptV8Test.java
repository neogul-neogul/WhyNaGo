package com.neogul.whynago.question.infra.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.EssayGradingTargetFixture;
import com.neogul.whynago.fixture.RubricFixture;
import com.neogul.whynago.fixture.SolvingTimeFixture;
import com.neogul.whynago.question.domain.EssayGradingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EssayPromptV8Test {

    private final EssayPromptV8 prompt = new EssayPromptV8();

    @Test
    @DisplayName("버전 문자열로 어떤 프롬프트가 쓰였는지 로그에서 구분할 수 있다.")
    void version() {
        assertThat(prompt.version()).isEqualTo("v8");
    }

    @ParameterizedTest
    @EnumSource(EssayGradingMode.class)
    @DisplayName("v7의 시스템 프롬프트와 판정 예시를 그대로 승계한다.")
    void systemPrompt_keepsV7(EssayGradingMode mode) {
        assertThat(prompt.systemPrompt(mode)).isEqualTo(new EssayPromptV7().systemPrompt(mode));
    }

    @Test
    @DisplayName("꼬리질문을 루브릭 판정 결과에 맞추도록 지시한다.")
    void userPrompt_missingRubricResultGuide() {
        String userPrompt = followupPrompt();

        assertThat(userPrompt).contains("criteriaResults를 채운 턴이라면 루브릭 판정을 기준으로 삼아라");
        assertThat(userPrompt).contains("met이 false인 항목이 있으면 그중 답변자가 가장 가까이 다가간 항목 하나만 골라");
        assertThat(userPrompt).contains("모든 항목이 met이면");
        // 항목 문장을 그대로 읽어 주면 답이 질문에 드러나 이해도 확인이 되지 않는다.
        assertThat(userPrompt).contains("항목의 문장을 그대로 읽어 주고 맞는지 되묻지 마라");
    }

    @Test
    @DisplayName("숙련도 등급마다 꼬리질문의 깊이를 지정한다.")
    void userPrompt_missingMasteryDepthGuide() {
        String userPrompt = followupPrompt();

        assertThat(userPrompt).contains("mastery 판정별로 꼬리질문의 깊이를 다음과 같이 맞춰라");
        assertThat(userPrompt)
                .contains("MASTERED:")
                .contains("SOLID:")
                .contains("UNSTABLE:")
                .contains("GUESSED:")
                .contains("WEAK:")
                .contains("NOT_LEARNED:");
        assertThat(userPrompt).contains("mastery가 GUESSED, WEAK, NOT_LEARNED 중 하나라면 어떤 경우에도 직전 답변보다 깊이를 더하지 마라");
    }

    @Test
    @DisplayName("모름 답변에는 채점 기준에서 끌어온, 정답을 주지 않는 실마리와 답할 수 있는 꼬리질문을 쓰도록 지시한다.")
    void userPrompt_guidesUnknownAnswerWithHint() {
        String userPrompt = followupPrompt();

        assertThat(userPrompt).contains("꼬리질문의 실마리를 이 문항의 채점 기준에서 끌어와라");
        assertThat(userPrompt).contains("충족하지 못한 항목 중 가장 기초적인 것 하나를 고른다");
        assertThat(userPrompt).contains("followupQuestion 앞에 붙인다");
        // 실마리가 정답이 되어 버리면 꼬리질문이 이해도를 확인하지 못한다.
        assertThat(userPrompt).contains("항목 문장을 그대로 옮기거나 용어의 정의를 그대로 주지 마라");
        assertThat(userPrompt).contains("이어지는 질문은 그 한 줄만으로 답할 수 있어야 한다");
        assertThat(userPrompt).contains("모른다고 한 용어를 다시 정의하라고 요구하거나, 새 심화 용어를 꺼내는 질문은 금지한다");
    }

    @Test
    @DisplayName("모름 답변의 꼬리질문은 서술문 한 문장과 질문 한 문장으로 쓰게 형식을 지정한다.")
    void userPrompt_missingHintSentenceFormat() {
        String userPrompt = followupPrompt();

        // 형식을 못 박지 않으면 실마리가 사라지고 질문 한 줄만 남는다. 내용 예시는 누출되므로 골격만 지정한다.
        assertThat(userPrompt).contains("이때의 followupQuestion은 반드시 두 문장으로 쓴다");
        assertThat(userPrompt).contains("첫 문장은 위에서 푼 한 줄이며, 묻지 말고 서술로 끝낸다. 물음표를 붙이지 마라");
        assertThat(userPrompt).contains("둘째 문장이 실제로 묻는 질문이고, 첫 문장이 말한 상황 안에서 답할 수 있어야 한다");
    }

    @Test
    @DisplayName("실마리는 모름 답변에만 쓰고 다른 문항의 문장을 옮겨 쓰지 못하게 막는다.")
    void userPrompt_missingHintScopeGuard() {
        String userPrompt = followupPrompt();

        assertThat(userPrompt).contains("이 방식은 모름을 밝힌 답변에만 쓴다");
        assertThat(userPrompt).contains("다른 문항에서 본 개념이나 문장을 지금 채점 대상에 옮겨 쓰지 마라");
        // 고정 예시를 두면 모델이 무관한 문항에 그 문장을 그대로 옮겨 쓴다. 예시 자체를 두지 않는다.
        assertThat(userPrompt).doesNotContain("짧은 대표값");
    }

    @Test
    @DisplayName("꼬리질문보다 채점 판정을 먼저 확정하게 해 개인화가 사후 합리화되지 않게 한다.")
    void userPrompt_missingJudgementOrder() {
        assertThat(followupPrompt())
                .contains("criteriaResults와 mastery를 먼저 확정한 뒤, 그 결과를 보고 followupQuestion을 만들어라");
    }

    @Test
    @DisplayName("v7의 꼬리질문 지시와 루브릭 범위 가드레일을 그대로 유지한다.")
    void userPrompt_keepsV7FollowupGuardrail() {
        String userPrompt = followupPrompt();

        assertThat(userPrompt).contains("또한 이 문답 흐름에 이어서 이해도를 더 깊이 확인할 꼬리질문 한 개를 한국어로 생성하라");
        assertThat(userPrompt).contains("직전 답변에서 모른다고 밝힌 용어는 꼬리질문에 다시 등장시키지 마라");
        assertThat(userPrompt).contains("꼬리질문은 다음 개념 범위 안에서 물어라: 흐름 제어, 혼잡 제어");
    }

    @Test
    @DisplayName("꼬리질문을 만들지 않는 턴에는 개인화 지시를 넣지 않는다.")
    void userPrompt_leaksGuideOnNoFollowupTurn() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.of(RubricFixture.threeCriteria(), SolvingTimeFixture.fast()),
                false);

        assertThat(userPrompt).doesNotContain("mastery 판정별로 꼬리질문의 깊이를");
        assertThat(userPrompt).contains("followupQuestion은 null로 두어라");
    }

    private String followupPrompt() {
        return prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.of(RubricFixture.threeCriteria(), SolvingTimeFixture.fast()),
                true);
    }
}
