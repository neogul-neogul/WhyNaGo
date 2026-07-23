package com.neogul.whynago.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.dto.EssayQnA;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GeneratedFollowup;
import com.neogul.whynago.question.infra.ai.GradedAnswer;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
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
    @DisplayName("본 질문 답변을 채점하고 다음 꼬리질문을 생성한다.")
    void evaluate() {
        // given
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        given(essayAiClient.grade(anyList())).willReturn(new GradedAnswer("피드백", "모범답안"));
        given(essayAiClient.generateFollowup(anyList())).willReturn(new GeneratedFollowup("꼬리질문1"));
        EvaluateEssayAnswerCommand command = new EvaluateEssayAnswerCommand(
                List.of(new EssayQnA(essay.getContent(), "제 답변입니다."))
        );

        // when
        EssayAnswerResult result = essayAnswerService.evaluate(essay.getId(), command);

        // then
        assertThat(result.grading().feedback()).isEqualTo("피드백");
        assertThat(result.grading().modelAnswer()).isEqualTo("모범답안");
        assertThat(result.nextFollowup().question()).isEqualTo("꼬리질문1");
    }

    @Test
    @DisplayName("마지막 문항(3번째)이면 꼬리질문을 생성하지 않는다.")
    void evaluate_lastTurnHasNoFollowup() {
        // given
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        given(essayAiClient.grade(anyList())).willReturn(new GradedAnswer("피드백", "모범답안"));
        EvaluateEssayAnswerCommand command = new EvaluateEssayAnswerCommand(List.of(
                new EssayQnA("본질문", "답변1"),
                new EssayQnA("꼬리질문1", "답변2"),
                new EssayQnA("꼬리질문2", "답변3")
        ));

        // when
        EssayAnswerResult result = essayAnswerService.evaluate(essay.getId(), command);

        // then
        assertThat(result.nextFollowup()).isNull();
        verify(essayAiClient, never()).generateFollowup(anyList());
    }

    @Test
    @DisplayName("서술형이 아닌 문제면 예외가 발생한다.")
    void evaluate_notEssay() {
        // given
        Question multipleChoice = questionRepository.save(QuestionFixture.rootMultipleChoice());
        EvaluateEssayAnswerCommand command = new EvaluateEssayAnswerCommand(
                List.of(new EssayQnA("질문", "답변"))
        );

        // when & then
        assertThatThrownBy(() -> essayAnswerService.evaluate(multipleChoice.getId(), command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_ESSAY));
    }

    @Test
    @DisplayName("존재하지 않는 문제면 예외가 발생한다.")
    void evaluate_questionNotFound() {
        // given
        EvaluateEssayAnswerCommand command = new EvaluateEssayAnswerCommand(
                List.of(new EssayQnA("질문", "답변"))
        );

        // when & then
        assertThatThrownBy(() -> essayAnswerService.evaluate(999L, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }
}
