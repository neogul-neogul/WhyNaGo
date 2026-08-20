package com.neogul.whynago.question.infra.ai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.EssayGradingTargetFixture;
import com.neogul.whynago.fixture.RubricFixture;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.RubricCriterion;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EssayPromptV5Test {

    private final EssayPromptV5 prompt = new EssayPromptV5();

    @ParameterizedTest
    @EnumSource(EssayGradingMode.class)
    @DisplayName("v4의 채점·숙련도 규칙을 그대로 승계한다.")
    void systemPrompt_keepsPreviousRules(EssayGradingMode mode) {
        assertThat(prompt.systemPrompt(mode)).isEqualTo(new EssayPromptV4().systemPrompt(mode));
    }

    @Test
    @DisplayName("루브릭 항목을 배점과 함께 1부터 번호를 붙여 내려준다.")
    void userPrompt_missingNumberedCriteria() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria()), true);

        assertThat(userPrompt).contains("1. (배점 3) TCP는 신뢰성 있는 데이터 전송이 필요한 경우에 사용된다.");
        assertThat(userPrompt).contains("2. (배점 3) UDP는 실시간 통신이나 저지연이 중요한 서비스에 사용된다.");
        assertThat(userPrompt).contains("3. (배점 4) TCP의 흐름 제어와 혼잡 제어가 처리 지연을 유발한다.");
    }

    @Test
    @DisplayName("항목마다 하나씩 판정을 채우고 배점 합을 점수로 쓰라고 지시한다.")
    void userPrompt_missingCriteriaResultsInstruction() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria()), true);

        assertThat(userPrompt).contains("criteriaResults에 항목 번호마다 정확히 한 개씩, 빠짐없이 담아라");
        assertThat(userPrompt).contains("score는 met이 true인 항목의 배점 합과 같아야 한다");
    }

    @Test
    @DisplayName("루브릭이 없으면 항목 판정을 비우라고 지시한다.")
    void userPrompt_withoutRubric() {
        String userPrompt = prompt.userPrompt(EssayGradingMode.PRACTICE, EssayGradingTargetFixture.plain(), true);

        assertThat(userPrompt).contains("criteriaResults는 빈 배열로 두어라");
        assertThat(userPrompt).doesNotContain("[채점 기준]");
    }

    @Test
    @DisplayName("항목이 비어 있는 루브릭은 루브릭이 없는 것과 같게 다룬다.")
    void userPrompt_emptyRubricTreatedAsPresent() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(new Rubric(List.of(), null)), true);

        assertThat(userPrompt).contains("criteriaResults는 빈 배열로 두어라");
        assertThat(userPrompt).doesNotContain("[채점 기준]");
    }

    @Test
    @DisplayName("꼬리질문 범위를 꼬리질문 지시에 함께 내려준다.")
    void userPrompt_missingFollowupScope() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria()), true);

        assertThat(userPrompt).contains("꼬리질문은 다음 개념 범위 안에서 물어라: 흐름 제어, 혼잡 제어");
        assertThat(userPrompt).contains("다음 영역으로는 넘어가지 마라: TCP/IP 계층 구조");
    }

    @Test
    @DisplayName("꼬리질문을 만들지 않는 턴에는 범위를 내려주지 않는다.")
    void userPrompt_scopeOnLastTurn() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria()), false);

        assertThat(userPrompt).contains("꼬리질문을 생성하지 말고");
        assertThat(userPrompt).doesNotContain("꼬리질문은 다음 개념 범위 안에서 물어라");
        // 루브릭 채점은 마지막 턴에도 해야 한다.
        assertThat(userPrompt).contains("[채점 기준]");
    }

    @Test
    @DisplayName("꼬리질문 범위가 없는 루브릭이면 범위 지시 없이 기본 꼬리질문 지시만 쓴다.")
    void userPrompt_missingScopeAddsEmptyLine() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(RubricFixture.withoutFollowupScope()), true);

        assertThat(userPrompt).doesNotContain("꼬리질문은 다음 개념 범위 안에서 물어라");
        assertThat(userPrompt).doesNotContain("다음 영역으로는 넘어가지 마라");
        assertThat(userPrompt).contains("[채점 기준]");
    }

    @Test
    @DisplayName("항목이 하나뿐인 루브릭도 번호를 붙여 내려준다.")
    void userPrompt_singleCriterion() {
        String userPrompt = prompt.userPrompt(
                EssayGradingMode.PRACTICE,
                EssayGradingTargetFixture.withRubric(
                        new Rubric(List.of(new RubricCriterion("단일 항목이다.", 10)), null)),
                true);

        assertThat(userPrompt).contains("1. (배점 10) 단일 항목이다.");
    }

    @Test
    @DisplayName("버전 문자열로 어떤 프롬프트가 쓰였는지 로그에서 구분할 수 있다.")
    void version() {
        assertThat(prompt.version()).isEqualTo("v5");
    }
}
