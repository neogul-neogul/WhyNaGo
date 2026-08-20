package com.neogul.whynago.question.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.EssayGradingTargetFixture;
import com.neogul.whynago.fixture.GradeAndFollowupResultFixture;
import com.neogul.whynago.fixture.RubricFixture;
import com.neogul.whynago.fixture.SolvingTimeFixture;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.SolvingTime;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.implement.dto.RubricEvaluation;
import com.neogul.whynago.question.infra.ai.CriterionGrading;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EssayAnswerEvaluatorTest {

    private static final String MET_REASON = "답변에서 그대로 짚었다.";
    private static final String UNMET_REASON = "그 내용이 빠졌다.";
    private static final EssayGradingTarget PLAIN = EssayGradingTargetFixture.plain();

    private final EssayAiClient essayAiClient = Mockito.mock(EssayAiClient.class);
    private final EssayAnswerEvaluator essayAnswerEvaluator =
            new EssayAnswerEvaluator(essayAiClient, new RubricGradingResolver());

    @Test
    @DisplayName("마지막 턴이 아니면 꼬리질문을 생성하고 대화를 정리하지 않는다.")
    void evaluate_generatesFollowupWhenNotLastTurn() {
        given(essayAiClient.completedTurns("conv")).willReturn(1);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 8, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE);

        assertThat(evaluation.followupQuestion()).isEqualTo("다음 꼬리질문");
        assertThat(evaluation.isCorrect()).isTrue();
        verify(essayAiClient).gradeAndGenerateFollowup("conv", PLAIN, true, EssayGradingMode.PRACTICE);
        verify(essayAiClient, never()).clearSession(anyString());
    }

    @Test
    @DisplayName("루브릭도 소요시간도 없으면 AI가 매긴 점수를 그대로 전달한다.")
    void evaluate_keepsScore() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 3, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(3);
        assertThat(evaluation.rubricCriteria()).isEmpty();
    }

    @Test
    @DisplayName("마지막 턴(완료 2턴)이면 꼬리질문을 생성하지 않고 대화를 정리한다.")
    void evaluate_lastTurnNoFollowupAndClears() {
        given(essayAiClient.completedTurns("conv")).willReturn(2);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 5, null));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE);

        assertThat(evaluation.followupQuestion()).isNull();
        verify(essayAiClient).gradeAndGenerateFollowup("conv", PLAIN, false, EssayGradingMode.PRACTICE);
        verify(essayAiClient).clearSession("conv");
    }

    @Test
    @DisplayName("전달받은 채점 모드를 그대로 AI 클라이언트에 넘긴다.")
    void evaluate_passesGradingMode() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 8, "다음 꼬리질문"));

        essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.INTERVIEW);

        verify(essayAiClient).gradeAndGenerateFollowup("conv", PLAIN, true, EssayGradingMode.INTERVIEW);
    }

    @Test
    @DisplayName("점수가 임계값(7) 이상이면 통과로 판정한다.")
    void evaluate_scoreAtThresholdPasses() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, "다음 꼬리질문"));

        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).isCorrect()).isTrue();
    }

    @Test
    @DisplayName("점수가 임계값(7) 미만이면 미통과로 판정한다.")
    void evaluate_scoreBelowThresholdFails() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 6, "다음 꼬리질문"));

        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).isCorrect()).isFalse();
    }

    @Test
    @DisplayName("본 질문 턴에는 문항의 루브릭을 AI에 그대로 내려보낸다.")
    void evaluate_rubricNotPassedOnFirstTurn() {
        EssayGradingTarget target = EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria());
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.withCriteria(0, allMet()));

        essayAnswerEvaluator.evaluate("conv", target, EssayGradingMode.PRACTICE);

        verify(essayAiClient).gradeAndGenerateFollowup("conv", target, true, EssayGradingMode.PRACTICE);
    }

    @Test
    @DisplayName("꼬리질문 턴에는 루브릭을 떼고 AI에 넘긴다.")
    void evaluate_rubricPassedOnFollowupTurn() {
        Rubric rubric = RubricFixture.threeCriteria();
        EssayGradingTarget target = EssayGradingTargetFixture.withRubric(rubric);
        given(essayAiClient.completedTurns("conv")).willReturn(1);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 8, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate("conv", target, EssayGradingMode.PRACTICE);

        assertThat(evaluation.rubricCriteria()).isEmpty();
        verify(essayAiClient).gradeAndGenerateFollowup(
                "conv", target.withoutRubric(), true, EssayGradingMode.PRACTICE);
    }

    @Test
    @DisplayName("루브릭이 있으면 충족한 항목의 배점 합을 점수로 쓰고 AI 점수는 무시한다.")
    void evaluate_usesAiScoreDespiteRubric() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.withCriteria(10, List.of(
                new CriterionGrading(1, true, MET_REASON),
                new CriterionGrading(2, true, MET_REASON),
                new CriterionGrading(3, false, UNMET_REASON))));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(6);
        assertThat(evaluation.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("루브릭 항목별 충족 여부와 그 근거를 함께 반환한다.")
    void evaluate_missingRubricCriteria() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.withCriteria(0, List.of(
                new CriterionGrading(1, true, MET_REASON),
                new CriterionGrading(2, false, UNMET_REASON),
                new CriterionGrading(3, false, UNMET_REASON))));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withRubric(RubricFixture.threeCriteria()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.rubricCriteria())
                .extracting(RubricEvaluation::weight, RubricEvaluation::met, RubricEvaluation::reason)
                .containsExactly(
                        tuple(3, true, MET_REASON),
                        tuple(3, false, UNMET_REASON),
                        tuple(4, false, UNMET_REASON));
        assertThat(evaluation.score()).isEqualTo(3);
    }

    @Test
    @DisplayName("평균보다 빠르게 답하면 점수를 1점 올린다.")
    void evaluate_ignoresFastPace() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 6, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withSolvingTime(SolvingTimeFixture.fast()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(7);
        assertThat(evaluation.isCorrect())
                .as("시간 가감이 통과 여부까지 바꾼다")
                .isTrue();
    }

    @Test
    @DisplayName("평균보다 오래 걸리면 점수를 1점 내린다.")
    void evaluate_ignoresSlowPace() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withSolvingTime(SolvingTimeFixture.slow()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(6);
        assertThat(evaluation.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("평균 수준의 시간이면 점수를 건드리지 않는다.")
    void evaluate_adjustsOnNormalPace() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withSolvingTime(SolvingTimeFixture.normal()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(7);
    }

    @Test
    @DisplayName("소요시간을 측정하지 못하면 점수를 건드리지 않는다.")
    void evaluate_adjustsOnUnmeasuredTime() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 5, "다음 꼬리질문"));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withSolvingTime(SolvingTimeFixture.unmeasured()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(5);
    }

    @Test
    @DisplayName("시간 가감을 얹어도 점수는 0~10을 벗어나지 않는다.")
    void evaluate_exceedsScoreRange() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 10, "다음 꼬리질문"));

        EssayEvaluation fast = essayAnswerEvaluator.evaluate(
                "conv", EssayGradingTargetFixture.withSolvingTime(SolvingTimeFixture.fast()),
                EssayGradingMode.PRACTICE);
        assertThat(fast.score()).isEqualTo(10);

        given(essayAiClient.completedTurns("conv2")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 0, "다음 꼬리질문"));
        EssayEvaluation slow = essayAnswerEvaluator.evaluate(
                "conv2", EssayGradingTargetFixture.withSolvingTime(SolvingTimeFixture.slow()),
                EssayGradingMode.PRACTICE);
        assertThat(slow.score()).isZero();
    }

    @Test
    @DisplayName("루브릭 배점 합에 시간 가감을 얹어 점수를 낸다.")
    void evaluate_appliesOnlyRubricScore() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        givenAiResult(GradeAndFollowupResultFixture.withCriteria(0, allMet()));

        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                "conv",
                EssayGradingTargetFixture.of(RubricFixture.threeCriteria(), SolvingTimeFixture.slow()),
                EssayGradingMode.PRACTICE);

        assertThat(evaluation.score())
                .as("전 항목 충족 10점에서 느린 시간으로 1점을 내린다")
                .isEqualTo(9);
        assertThat(evaluation.solvingTime().scoreAdjustment()).isEqualTo(-1);
    }

    @Test
    @DisplayName("대화 이력의 완료 턴 수로 몇 번째 턴인지 정한다.")
    void evaluate_carriesTurn() {
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 8, "다음 꼬리질문"));

        given(essayAiClient.completedTurns("conv")).willReturn(0);
        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).turn()).isEqualTo(1);

        given(essayAiClient.completedTurns("conv")).willReturn(1);
        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).turn()).isEqualTo(2);

        given(essayAiClient.completedTurns("conv")).willReturn(2);
        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).turn()).isEqualTo(3);
    }

    @Test
    @DisplayName("첫 턴만 본질문으로 판정한다.")
    void evaluate_rootTurnOnlyOnFirstTurn() {
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 8, "다음 꼬리질문"));

        given(essayAiClient.completedTurns("conv")).willReturn(0);
        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).isRootTurn()).isTrue();

        given(essayAiClient.completedTurns("conv")).willReturn(1);
        assertThat(essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE).isRootTurn()).isFalse();
    }

    @Test
    @DisplayName("꼬리질문 턴은 루트 문항의 평균 소요시간을 기준으로 쓰지 않는다.")
    void evaluate_followupTurnDropsRootBaseline() {
        // given - 루트 문항 평균이 30초라 100초는 느림(-1)이지만,
        // 꼬리질문에는 그 발문의 평균이 없으므로 기본 180초 대비 빠름(+1)이 되어야 한다.
        EssayGradingTarget target =
                EssayGradingTargetFixture.withSolvingTime(SolvingTime.of(100, 30, 10));
        givenAiResult(GradeAndFollowupResultFixture.of("피드백", "모범답안", 6, "다음 꼬리질문"));

        given(essayAiClient.completedTurns("conv")).willReturn(0);
        EssayEvaluation mainTurn = essayAnswerEvaluator.evaluate("conv", target, EssayGradingMode.PRACTICE);

        given(essayAiClient.completedTurns("conv")).willReturn(1);
        EssayEvaluation followup = essayAnswerEvaluator.evaluate("conv", target, EssayGradingMode.PRACTICE);

        // then
        assertThat(mainTurn.solvingTime().pace()).isEqualTo(ElapsedPace.SLOW);
        assertThat(mainTurn.score()).isEqualTo(5);
        assertThat(followup.solvingTime().pace())
                .as("다른 질문의 평균으로 빠름·느림을 판정하면 안 된다")
                .isEqualTo(ElapsedPace.FAST);
        assertThat(followup.score()).isEqualTo(7);
    }

    @Test
    @DisplayName("AI 호출이 실패하면 도메인 예외를 그대로 전파한다.")
    void evaluate_aiFailure() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(
                anyString(), any(EssayGradingTarget.class), anyBoolean(), any(EssayGradingMode.class)))
                .willThrow(new BusinessException(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));

        assertThatThrownBy(() -> essayAnswerEvaluator.evaluate("conv", PLAIN, EssayGradingMode.PRACTICE))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }

    private void givenAiResult(GradeAndFollowupResult result) {
        given(essayAiClient.gradeAndGenerateFollowup(
                anyString(), any(EssayGradingTarget.class), anyBoolean(), any(EssayGradingMode.class)))
                .willReturn(result);
    }

    private static List<CriterionGrading> allMet() {
        return List.of(
                new CriterionGrading(1, true, MET_REASON),
                new CriterionGrading(2, true, MET_REASON),
                new CriterionGrading(3, true, MET_REASON));
    }
}
