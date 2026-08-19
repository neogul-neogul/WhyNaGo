package com.neogul.whynago.question.infra.ai;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.neogul.whynago.fixture.GradeAndFollowupResultFixture;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.exception.ErrorCode;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.retry.NonTransientAiException;

class GeminiEssayAiClientTest {

    private static final String CONVERSATION_ID = "conv-1";
    private static final String PROMPT_VERSION = "v-test";
    private static final String SYSTEM_PROMPT = "시스템 프롬프트";
    private static final String USER_PROMPT = "사용자 프롬프트";

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatMemory chatMemory;
    private EssayPrompt essayPrompt;
    private GeminiEssayAiClient client;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_SELF);
        given(builder.build()).willReturn(chatClient);
        chatMemory = mock(ChatMemory.class);
        given(chatMemory.get(anyString())).willReturn(List.of());
        essayPrompt = mock(EssayPrompt.class);
        given(essayPrompt.version()).willReturn(PROMPT_VERSION);
        given(essayPrompt.systemPrompt(any(EssayGradingMode.class))).willReturn(SYSTEM_PROMPT);
        given(essayPrompt.userPrompt(any(EssayGradingMode.class), anyString(), anyString(), anyBoolean()))
                .willReturn(USER_PROMPT);
        client = new GeminiEssayAiClient(builder, chatMemory, essayPrompt);

        // 체인 각 단계가 같은 spec을 돌려주게 해, 전달된 프롬프트를 한 객체에서 검증한다.
        requestSpec = chatClient.prompt();
        given(requestSpec.system(anyString())).willReturn(requestSpec);
        given(requestSpec.advisors(any(Consumer.class))).willReturn(requestSpec);
        given(requestSpec.user(anyString())).willReturn(requestSpec);

        logAppender = new ListAppender<>();
        logAppender.start();
        clientLogger().addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        clientLogger().detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    @DisplayName("채점에 성공하면 응답 본문을 반환한다.")
    void grade() {
        // given
        GradeAndFollowupResult expected = GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, "꼬리질문");
        givenAiCallReturns(expected, chatResponseWith("gemini-3.5-flash-lite", new DefaultUsage(120, 45, 165)));

        // when
        GradeAndFollowupResult result = client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.PRACTICE);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("채점에 성공하면 사용한 모델과 프롬프트 버전, 토큰 사용량, 호출 소요 시간을 로그로 남긴다.")
    void grade_logsTokenUsage() {
        // given
        givenAiCallReturns(
                GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, null),
                chatResponseWith("gemini-3.5-flash-lite", new DefaultUsage(120, 45, 165)));

        // when
        client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", false, EssayGradingMode.PRACTICE);

        // then
        assertThat(logAppender.list)
                .singleElement()
                .extracting(ILoggingEvent::getFormattedMessage, as(STRING))
                .contains("model=gemini-3.5-flash-lite")
                .contains("promptVersion=" + PROMPT_VERSION)
                .contains("promptTokens=120")
                .contains("completionTokens=45")
                .contains("totalTokens=165")
                .containsPattern("elapsedMs=\\d+");
    }

    @Test
    @DisplayName("프롬프트 버전이 만든 시스템·사용자 프롬프트를 그대로 호출에 담는다.")
    void grade_delegatesPromptToEssayPrompt() {
        // given
        givenAiCallReturns(
                GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, "꼬리질문"),
                chatResponseWith("gemini-3.5-flash-lite", new DefaultUsage(120, 45, 165)));

        // when
        client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.INTERVIEW);

        // then
        verify(essayPrompt).systemPrompt(EssayGradingMode.INTERVIEW);
        verify(essayPrompt).userPrompt(EssayGradingMode.INTERVIEW, "질문", "답변", true);
        assertThat(capturedSystemPrompt()).isEqualTo(SYSTEM_PROMPT);
        assertThat(capturedUserText()).isEqualTo(USER_PROMPT);
    }

    @Test
    @DisplayName("꼬리질문을 만들지 않는 턴은 프롬프트 생성에도 그대로 전달한다.")
    void grade_delegatesNoFollowupTurn() {
        // given
        givenAiCallReturns(
                GradeAndFollowupResultFixture.of("피드백", "모범답안", 7, null),
                chatResponseWith("gemini-3.5-flash-lite", new DefaultUsage(120, 45, 165)));

        // when
        client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", false, EssayGradingMode.PRACTICE);

        // then
        verify(essayPrompt).userPrompt(EssayGradingMode.PRACTICE, "질문", "답변", false);
    }

    @Test
    @DisplayName("Gemini 호출이 실패하면 도메인 에러코드로 변환한다.")
    void grade_aiCallFails() {
        RuntimeException llmFailure = new RuntimeException("LLM down");
        givenAiCallFailsWith(llmFailure);

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.PRACTICE))
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

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.PRACTICE))
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

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.PRACTICE))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(errorCodeOf(exception))
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_DAILY_QUOTA_EXCEEDED));
    }

    @Test
    @DisplayName("쿼터와 무관한 4xx 실패는 쿼터 초과로 판정하지 않는다.")
    void grade_nonQuotaClientError() {
        givenAiCallFailsWith(new NonTransientAiException(
                "HTTP 401 - {\"error\":{\"code\":401,\"status\":\"UNAUTHENTICATED\"}}"));

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.PRACTICE))
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

        assertThatThrownBy(() -> client.gradeAndGenerateFollowup(CONVERSATION_ID, "질문", "답변", true, EssayGradingMode.PRACTICE))
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

    private void givenAiCallFailsWith(RuntimeException exception) {
        given(requestSpec.call().responseEntity(GradeAndFollowupResult.class)).willThrow(exception);
    }

    private void givenAiCallReturns(GradeAndFollowupResult entity, ChatResponse response) {
        given(requestSpec.call().responseEntity(GradeAndFollowupResult.class))
                .willReturn(new ResponseEntity<>(response, entity));
    }

    private String capturedSystemPrompt() {
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        verify(requestSpec, atLeastOnce()).system(systemPrompt.capture());
        return systemPrompt.getValue();
    }

    private String capturedUserText() {
        ArgumentCaptor<String> userText = ArgumentCaptor.forClass(String.class);
        verify(requestSpec, atLeastOnce()).user(userText.capture());
        return userText.getValue();
    }

    private static ChatResponse chatResponseWith(String model, Usage usage) {
        return ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage("응답"))))
                .metadata(ChatResponseMetadata.builder().model(model).usage(usage).build())
                .build();
    }

    private ErrorCode errorCodeOf(Throwable exception) {
        return ((BusinessException) exception).errorCode();
    }

    private static Logger clientLogger() {
        return (Logger) LoggerFactory.getLogger(GeminiEssayAiClient.class);
    }
}
