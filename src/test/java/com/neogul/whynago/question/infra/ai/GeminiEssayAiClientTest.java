package com.neogul.whynago.question.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.exception.ErrorCode;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.retry.NonTransientAiException;

class GeminiEssayAiClientTest {

    private static final String CONVERSATION_ID = "conv-1";

    private ChatClient chatClient;
    private ChatMemory chatMemory;
    private GeminiEssayAiClient client;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_SELF);
        given(builder.build()).willReturn(chatClient);
        chatMemory = mock(ChatMemory.class);
        given(chatMemory.get(anyString())).willReturn(List.of());
        client = new GeminiEssayAiClient(builder, chatMemory);
    }

    @Test
    @DisplayName("Gemini 호출이 실패하면 도메인 에러코드로 변환한다.")
    void grade_aiCallFails() {
        RuntimeException llmFailure = new RuntimeException("LLM down");
        givenAiCallFailsWith(llmFailure);

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true))
                .isInstanceOf(BusinessException.class)
                .hasCause(llmFailure)
                .satisfies(exception -> assertThat(errorCodeOf(exception))
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }

    @Test
    @DisplayName("분당 쿼터를 초과하면 쿼터 초과 에러코드로 변환한다.")
    void grade_quotaExceeded() {
        givenAiCallFailsWith(new NonTransientAiException(
                "HTTP 429 - {\"error\":{\"code\":429,\"status\":\"RESOURCE_EXHAUSTED\","
                        + "\"message\":\"Quota exceeded for quota metric 'Generate requests per minute'\"}}"));

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(errorCodeOf(exception))
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_QUOTA_EXCEEDED));
    }

    @Test
    @DisplayName("일일 쿼터를 초과하면 일일 쿼터 초과 에러코드로 변환한다.")
    void grade_dailyQuotaExceeded() {
        givenAiCallFailsWith(new NonTransientAiException(
                "HTTP 429 - {\"error\":{\"code\":429,\"status\":\"RESOURCE_EXHAUSTED\",\"details\":"
                        + "[{\"quotaId\":\"GenerateRequestsPerDayPerProjectPerModel-FreeTier\"}]}}"));

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(errorCodeOf(exception))
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_DAILY_QUOTA_EXCEEDED));
    }

    @Test
    @DisplayName("쿼터와 무관한 4xx 실패는 쿼터 초과로 판정하지 않는다.")
    void grade_nonQuotaClientError() {
        givenAiCallFailsWith(new NonTransientAiException(
                "HTTP 401 - {\"error\":{\"code\":401,\"status\":\"UNAUTHENTICATED\"}}"));

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(errorCodeOf(exception))
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));
    }

    @Test
    @DisplayName("채점이 실패하면 대화 메모리를 호출 직전 상태로 되돌린다.")
    void grade_rollsBackMemory() {
        List<Message> beforeCall = List.of(new UserMessage("답변1"), new AssistantMessage("피드백1"));
        given(chatMemory.get(CONVERSATION_ID)).willReturn(beforeCall);
        givenAiCallFailsWith(new RuntimeException("LLM down"));

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true))
                .isInstanceOf(BusinessException.class);

        verify(chatMemory).clear(CONVERSATION_ID);
        verify(chatMemory).add(CONVERSATION_ID, beforeCall);
    }

    @Test
    @DisplayName("완료한 턴 수는 응답이 돌아온 턴만 계산한다.")
    void completedTurns() {
        given(chatMemory.get(CONVERSATION_ID)).willReturn(List.of(
                new UserMessage("답변1"),
                new AssistantMessage("피드백1"),
                new UserMessage("답변2")));

        assertThat(client.completedTurns(CONVERSATION_ID)).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    private void givenAiCallFailsWith(RuntimeException exception) {
        given(chatClient.prompt()
                .advisors(any(Consumer.class))
                .user(anyString())
                .call()
                .entity(GradeAndFollowupResult.class))
                .willThrow(exception);
    }

    private ErrorCode errorCodeOf(Throwable exception) {
        return ((BusinessException) exception).errorCode();
    }
}
