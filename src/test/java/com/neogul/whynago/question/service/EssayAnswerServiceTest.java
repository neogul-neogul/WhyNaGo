package com.neogul.whynago.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.EssaySessionResult;
import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import com.neogul.whynago.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class EssayAnswerServiceTest extends IntegrationTestSupport {

    @Autowired
    private EssayAnswerService essayAnswerService;

    @Autowired
    private QuestionRepository questionRepository;

    @MockitoBean
    private EssayAiClient essayAiClient;

    @Test
    @DisplayName("서술형 세션을 시작하면 대화 식별자를 발급한다.")
    void startSession() {
        Question essay = questionRepository.save(QuestionFixture.essayRoot());

        EssaySessionResult result = essayAnswerService.startSession(essay.getId());

        assertThat(result.conversationId()).isNotBlank();
    }

    @Test
    @DisplayName("서술형이 아닌 문제로 세션을 시작하면 예외가 발생한다.")
    void startSession_notEssay() {
        Question multipleChoice = questionRepository.save(QuestionFixture.rootMultipleChoice());

        assertThatThrownBy(() -> essayAnswerService.startSession(multipleChoice.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_ESSAY));
    }

    @Test
    @DisplayName("답변을 채점하고 다음 꼬리질문과 통과 여부를 응답한다.")
    void evaluate() {
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        given(essayAiClient.completedTurns(anyString())).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(anyString(), anyString(), anyString(), anyBoolean()))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 9, "꼬리질문1"));
        EvaluateEssayAnswerCommand command =
                new EvaluateEssayAnswerCommand("conv-1", essay.getContent(), "제 답변입니다.");

        EssayAnswerResult result = essayAnswerService.evaluate(essay.getId(), command);

        assertThat(result.grading().feedback()).isEqualTo("피드백");
        assertThat(result.grading().modelAnswer()).isEqualTo("모범답안");
        assertThat(result.grading().isCorrect()).isTrue();
        assertThat(result.nextFollowup().question()).isEqualTo("꼬리질문1");
    }

    @Test
    @DisplayName("마지막 문항(완료 2턴)이면 꼬리질문을 생성하지 않는다.")
    void evaluate_lastTurnHasNoFollowup() {
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        given(essayAiClient.completedTurns(anyString())).willReturn(2);
        given(essayAiClient.gradeAndGenerateFollowup(anyString(), anyString(), anyString(), anyBoolean()))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 5, null));
        EvaluateEssayAnswerCommand command =
                new EvaluateEssayAnswerCommand("conv-1", "꼬리질문2", "답변3");

        EssayAnswerResult result = essayAnswerService.evaluate(essay.getId(), command);

        assertThat(result.nextFollowup()).isNull();
        assertThat(result.grading().isCorrect()).isFalse();
        verify(essayAiClient).gradeAndGenerateFollowup(anyString(), anyString(), anyString(), eq(false));
    }

    @Test
    @DisplayName("서술형이 아닌 문제면 예외가 발생한다.")
    void evaluate_notEssay() {
        Question multipleChoice = questionRepository.save(QuestionFixture.rootMultipleChoice());
        EvaluateEssayAnswerCommand command = new EvaluateEssayAnswerCommand("conv-1", "질문", "답변");

        assertThatThrownBy(() -> essayAnswerService.evaluate(multipleChoice.getId(), command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_ESSAY));
    }

    @Test
    @DisplayName("존재하지 않는 문제면 예외가 발생한다.")
    void evaluate_questionNotFound() {
        EvaluateEssayAnswerCommand command = new EvaluateEssayAnswerCommand("conv-1", "질문", "답변");

        assertThatThrownBy(() -> essayAnswerService.evaluate(999L, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }
}
