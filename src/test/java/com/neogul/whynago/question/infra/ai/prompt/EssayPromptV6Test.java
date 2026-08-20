package com.neogul.whynago.question.infra.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.EssayGradingTargetFixture;
import com.neogul.whynago.fixture.RubricFixture;
import com.neogul.whynago.fixture.SolvingTimeFixture;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.SolvingTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EssayPromptV6Test {

    private final EssayPromptV6 prompt = new EssayPromptV6();

    @Test
    @DisplayName("소요시간과 문항 평균을 함께 내려준다.")
    void userPrompt_missingElapsedSeconds() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withSolvingTime(SolvingTime.of(240, 400, 10)),
                true);

        assertThat(userPrompt).contains("[소요시간]");
        assertThat(userPrompt).contains("이 답변을 쓰는 데 240초가 걸렸다. 이 문항의 평균 소요시간은 400초이며");
    }

    @Test
    @DisplayName("평균 대비 빠름·보통·느림을 문장으로 알려준다.")
    void userPrompt_missingPaceText() {
        assertThat(userPromptWith(SolvingTimeFixture.fast())).contains("평균 대비 뚜렷하게 빠르다.");
        assertThat(userPromptWith(SolvingTimeFixture.normal())).contains("평균 대비 보통 수준이다.");
        assertThat(userPromptWith(SolvingTimeFixture.slow())).contains("평균 대비 뚜렷하게 오래 걸렸다.");
    }

    @Test
    @DisplayName("시간은 숙련도와 피드백에만 반영하고 점수는 건드리지 말라고 지시한다.")
    void userPrompt_missingScoreGuard() {
        String userPrompt = userPromptWith(SolvingTimeFixture.slow());

        assertThat(userPrompt).contains("소요시간은 mastery 판정과 feedback에만 반영하라");
        assertThat(userPrompt).contains("score는 시간으로 조정하지 마라");
        // 오래 걸린 것을 성실함으로 읽어 등급을 올리는 오해를 막는 지시가 빠지면 판정이 뒤집힌다.
        assertThat(userPrompt).contains("느린 것 자체를 성실함으로 읽지 마라");
    }

    @Test
    @DisplayName("소요시간을 측정하지 못하면 시간을 근거로 삼지 말라고 지시한다.")
    void userPrompt_unmeasuredTime() {
        String userPrompt = userPromptWith(SolvingTimeFixture.unmeasured());

        assertThat(userPrompt).contains("소요시간은 측정되지 않았다");
        assertThat(userPrompt).doesNotContain("[소요시간]");
    }

    @ParameterizedTest
    @EnumSource(EssayGradingMode.class)
    @DisplayName("v5와 달리 숙련도 판정에 시간을 보조 근거로 쓰도록 지시한다.")
    void systemPrompt_keepsContentOnlyMastery(EssayGradingMode mode) {
        String systemPrompt = prompt.systemPrompt(mode);

        assertThat(systemPrompt).contains("1차 근거는 답변에 실제로 쓰인 내용이고, 소요시간은 그 위에 얹는 보조 근거다");
        assertThat(systemPrompt).contains("시간이 빠르다는 것만으로 등급을 올리지도 마라");
        assertThat(systemPrompt).isNotEqualTo(new EssayPromptV5().systemPrompt(mode));
    }

    @Test
    @DisplayName("v5의 루브릭 채점 지시를 그대로 승계한다.")
    void userPrompt_keepsRubricInstruction() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.of(RubricFixture.threeCriteria(), SolvingTimeFixture.fast()),
                true);

        assertThat(userPrompt).contains("[채점 기준]");
        assertThat(userPrompt).contains("1. (배점 3) TCP는 신뢰성 있는 데이터 전송이 필요한 경우에 사용된다.");
        assertThat(userPrompt).contains("criteriaResults에 항목 번호마다 정확히 한 개씩, 빠짐없이 담아라");
        assertThat(userPrompt).contains("꼬리질문은 다음 개념 범위 안에서 물어라: 흐름 제어, 혼잡 제어");
    }

    @Test
    @DisplayName("버전 문자열로 어떤 프롬프트가 쓰였는지 로그에서 구분할 수 있다.")
    void version() {
        assertThat(prompt.version()).isEqualTo("v6");
    }

    private String userPromptWith(SolvingTime solvingTime) {
        return prompt.userPrompt(
                EssayGradingMode.PRACTICE, EssayGradingTargetFixture.withSolvingTime(solvingTime), true);
    }
}
