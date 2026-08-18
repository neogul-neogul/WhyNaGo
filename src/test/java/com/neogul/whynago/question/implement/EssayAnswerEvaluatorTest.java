package com.neogul.whynago.question.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EssayAnswerEvaluatorTest {

    private final EssayAiClient essayAiClient = Mockito.mock(EssayAiClient.class);
    private final EssayAnswerEvaluator essayAnswerEvaluator = new EssayAnswerEvaluator(essayAiClient);

    @Test
    @DisplayName("마지막 턴이 아니면 꼬리질문을 생성하고 대화를 정리하지 않는다.")
    void evaluate_generatesFollowupWhenNotLastTurn() {
        given(essayAiClient.completedTurns("conv")).willReturn(1);
        given(essayAiClient.gradeAndGenerateFollowup("conv", "질문", "답변", true, EssayGradingMode.PRACTICE))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 8, "다음 꼬리질문"));

        EssayEvaluation evaluation =
                essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE);

        assertThat(evaluation.followupQuestion()).isEqualTo("다음 꼬리질문");
        assertThat(evaluation.isCorrect()).isTrue();
        verify(essayAiClient).gradeAndGenerateFollowup("conv", "질문", "답변", true, EssayGradingMode.PRACTICE);
        verify(essayAiClient, never()).clearSession(anyString());
    }

    @Test
    @DisplayName("AI가 매긴 점수를 버리지 않고 그대로 전달한다.")
    void evaluate_keepsScore() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup("conv", "질문", "답변", true, EssayGradingMode.PRACTICE))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 3, "다음 꼬리질문"));

        EssayEvaluation evaluation =
                essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE);

        assertThat(evaluation.score()).isEqualTo(3);
    }

    @Test
    @DisplayName("점수가 7점 미만이면 오답으로 판정한다.")
    void evaluate_belowPassThreshold() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup("conv", "질문", "답변", true, EssayGradingMode.PRACTICE))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 6, "다음 꼬리질문"));

        EssayEvaluation evaluation =
                essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE);

        assertThat(evaluation.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("마지막 턴(완료 2턴)이면 꼬리질문을 생성하지 않고 대화를 정리한다.")
    void evaluate_lastTurnNoFollowupAndClears() {
        given(essayAiClient.completedTurns("conv")).willReturn(2);
        given(essayAiClient.gradeAndGenerateFollowup("conv", "질문", "답변", false, EssayGradingMode.PRACTICE))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 5, null));

        EssayEvaluation evaluation =
                essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE);

        assertThat(evaluation.followupQuestion()).isNull();
        verify(essayAiClient).gradeAndGenerateFollowup("conv", "질문", "답변", false, EssayGradingMode.PRACTICE);
        verify(essayAiClient).clearSession("conv");
    }

    @Test
    @DisplayName("전달받은 채점 모드를 그대로 AI 클라이언트에 넘긴다.")
    void evaluate_passesGradingMode() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(
                anyString(), anyString(), anyString(), anyBoolean(), any(EssayGradingMode.class)))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 8, "다음 꼬리질문"));

        essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.INTERVIEW);

        verify(essayAiClient).gradeAndGenerateFollowup("conv", "질문", "답변", true, EssayGradingMode.INTERVIEW);
    }

    @Test
    @DisplayName("점수가 임계값(7) 이상이면 통과로 판정한다.")
    void evaluate_scoreAtThresholdPasses() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(
                eq("conv"), eq("질문"), eq("답변"), anyBoolean(), eq(EssayGradingMode.PRACTICE)))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 7, "다음 꼬리질문"));

        assertThat(essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE).isCorrect())
                .isTrue();
    }

    @Test
    @DisplayName("점수가 임계값(7) 미만이면 미통과로 판정한다.")
    void evaluate_scoreBelowThresholdFails() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(
                eq("conv"), eq("질문"), eq("답변"), anyBoolean(), eq(EssayGradingMode.PRACTICE)))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 6, "다음 꼬리질문"));

        assertThat(essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE).isCorrect())
                .isFalse();
    }

    @Test
    @DisplayName("AI 호출이 실패하면 도메인 예외를 그대로 전파한다.")
    void evaluate_aiFailure() {
        given(essayAiClient.completedTurns("conv")).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(
                anyString(), anyString(), anyString(), anyBoolean(), any(EssayGradingMode.class)))
                .willThrow(new BusinessException(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));

        assertThatThrownBy(() -> essayAnswerEvaluator.evaluate("conv", "질문", "답변", EssayGradingMode.PRACTICE))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }
}
