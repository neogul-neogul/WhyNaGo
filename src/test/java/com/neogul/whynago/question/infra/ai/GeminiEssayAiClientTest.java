package com.neogul.whynago.question.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

class GeminiEssayAiClientTest {

    @Test
    @DisplayName("Gemini 호출이 실패하면 도메인 에러코드로 변환한다.")
    void grade_aiCallFails() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_SELF);
        given(builder.build()).willReturn(chatClient);
        ChatMemory chatMemory = mock(ChatMemory.class);
        given(chatClient.prompt()
                .advisors(any(Consumer.class))
                .user(anyString())
                .call()
                .entity(GradeAndFollowupResult.class))
                .willThrow(new RuntimeException("LLM down"));

        GeminiEssayAiClient client = new GeminiEssayAiClient(builder, chatMemory);

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup("conv-1", "질문", "답변", true))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }
}
