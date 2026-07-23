package com.neogul.whynago.question.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class GeminiEssayAiClientTest {

    @Test
    @DisplayName("Gemini 호출이 실패하면 도메인 에러코드로 변환한다.")
    void grade_aiCallFails() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        given(builder.build()).willReturn(chatClient);
        given(chatClient.prompt().user(anyString()).call().entity(GradedAnswer.class))
                .willThrow(new RuntimeException("LLM down"));

        GeminiEssayAiClient client = new GeminiEssayAiClient(builder);

        assertThatThrownBy(() -> client.grade(List.of(new EssayTurn("질문", "답변"))))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }
}
