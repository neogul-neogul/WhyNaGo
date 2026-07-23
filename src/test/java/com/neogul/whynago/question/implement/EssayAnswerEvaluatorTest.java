package com.neogul.whynago.question.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.dto.EssayQnA;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EssayAnswerEvaluatorTest {

    private final EssayAiClient essayAiClient = mock(EssayAiClient.class);
    private final EssayAnswerEvaluator essayAnswerEvaluator = new EssayAnswerEvaluator(essayAiClient);

    @Test
    @DisplayName("AI 호출이 실패하면 CompletionException을 벗겨 도메인 예외를 전파한다.")
    void evaluate_aiFailure() {
        given(essayAiClient.grade(anyList()))
                .willThrow(new BusinessException(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));

        assertThatThrownBy(() -> essayAnswerEvaluator.evaluate(List.of(new EssayQnA("질문", "답변"))))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }
}
