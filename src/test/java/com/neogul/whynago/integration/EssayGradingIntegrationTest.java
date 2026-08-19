package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.service.EssayAnswerService;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.EssaySessionResult;
import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 서술형 채점 흐름을 ChatMemory까지 실제로 태워 검증한다.
 * LLM 전송 계층인 ChatModel만 대체하고 ChatClient·MessageChatMemoryAdvisor·ChatMemory는 실제 빈을 사용한다.
 */
// 채점 흐름을 실제로 태우려면 Mock 클라이언트가 아니라 GeminiEssayAiClient가 떠 있어야 한다.
// 그 빈은 whynago.ai.enabled로 열리고, 이 값을 기본 테스트 설정에 두면 다른 통합 테스트까지
// 실제 클라이언트를 물게 되므로 이 테스트에서만 켠다.
@TestPropertySource(properties = "whynago.ai.enabled=true")
class EssayGradingIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private EssayAnswerService essayAnswerService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ChatMemory chatMemory;

    @MockitoBean
    private ChatModel chatModel;

    private Question essay;

    @BeforeEach
    void setUp() {
        essay = questionRepository.save(QuestionFixture.essayRoot());
        given(chatModel.getDefaultOptions()).willReturn(ChatOptions.builder().build());
    }

    @Test
    @DisplayName("본질문과 꼬리질문 2개를 이어 풀면 이전 문답이 대화 이력으로 누적되고 마지막 턴에는 꼬리질문을 생성하지 않는다.")
    void gradeThreeTurns() {
        // given
        given(chatModel.call(any(Prompt.class))).willReturn(
                aiResponse("피드백1", 9, "꼬리질문1"),
                aiResponse("피드백2", 8, "꼬리질문2"),
                aiResponse("피드백3", 4, null)
        );
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult first = evaluate(conversationId, essay.getContent(), "답변1");
        EssayAnswerResult second = evaluate(conversationId, first.nextFollowup().question(), "답변2");
        EssayAnswerResult third = evaluate(conversationId, second.nextFollowup().question(), "답변3");

        // then
        assertThat(first.grading().feedback()).isEqualTo("피드백1");
        assertThat(first.grading().isCorrect()).isTrue();
        assertThat(first.nextFollowup().question()).isEqualTo("꼬리질문1");
        assertThat(second.nextFollowup().question()).isEqualTo("꼬리질문2");
        assertThat(third.grading().isCorrect()).isFalse();
        assertThat(third.nextFollowup()).isNull();

        List<Prompt> prompts = capturedPrompts(3);
        assertThat(promptText(prompts.get(0)))
                .contains("답변1")
                .doesNotContain("답변2", "답변3");
        assertThat(promptText(prompts.get(2)))
                .as("마지막 턴 프롬프트에는 이전 두 턴의 문답이 이력으로 실려야 한다")
                .contains("답변1", "꼬리질문1", "답변2", "꼬리질문2", "답변3");
    }

    @Test
    @DisplayName("마지막 턴 이전에는 꼬리질문 생성을 지시하고 마지막 턴에는 생성하지 말라고 지시한다.")
    void gradeThreeTurns_followupInstructionPerTurn() {
        // given
        given(chatModel.call(any(Prompt.class))).willReturn(
                aiResponse("피드백1", 9, "꼬리질문1"),
                aiResponse("피드백2", 8, "꼬리질문2"),
                aiResponse("피드백3", 7, null)
        );
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult first = evaluate(conversationId, essay.getContent(), "답변1");
        EssayAnswerResult second = evaluate(conversationId, first.nextFollowup().question(), "답변2");
        evaluate(conversationId, second.nextFollowup().question(), "답변3");

        // then
        List<Prompt> prompts = capturedPrompts(3);
        assertThat(currentUserText(prompts.get(0))).contains("꼬리질문 한 개를");
        assertThat(currentUserText(prompts.get(1))).contains("꼬리질문 한 개를");
        assertThat(currentUserText(prompts.get(2))).contains("꼬리질문을 생성하지 말고");
    }

    @Test
    @DisplayName("3턴을 모두 채점하면 대화 이력을 비운다.")
    void gradeThreeTurns_clearsMemory() {
        // given
        given(chatModel.call(any(Prompt.class))).willReturn(
                aiResponse("피드백1", 9, "꼬리질문1"),
                aiResponse("피드백2", 8, "꼬리질문2"),
                aiResponse("피드백3", 6, null)
        );
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult first = evaluate(conversationId, essay.getContent(), "답변1");
        assertThat(chatMemory.get(conversationId)).isNotEmpty();
        EssayAnswerResult second = evaluate(conversationId, first.nextFollowup().question(), "답변2");
        evaluate(conversationId, second.nextFollowup().question(), "답변3");

        // then
        assertThat(chatMemory.get(conversationId)).isEmpty();
    }

    @Test
    @DisplayName("채점이 실패한 턴은 대화 이력에 남지 않아 재시도해도 꼬리질문이 조기에 끊기지 않는다.")
    void gradeAfterFailure_doesNotShortenSession() {
        // given
        given(chatModel.call(any(Prompt.class)))
                .willReturn(aiResponse("피드백1", 9, "꼬리질문1"))
                .willThrow(new NonTransientAiException(
                        "HTTP 429 - {\"error\":{\"code\":429,\"status\":\"RESOURCE_EXHAUSTED\"}}"))
                .willReturn(aiResponse("피드백2", 8, "꼬리질문2"));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();
        EssayAnswerResult first = evaluate(conversationId, essay.getContent(), "답변1");

        // when
        assertThatThrownBy(() -> evaluate(conversationId, first.nextFollowup().question(), "답변2"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.ESSAY_AI_QUOTA_EXCEEDED));
        EssayAnswerResult retried = evaluate(conversationId, first.nextFollowup().question(), "답변2");

        // then
        assertThat(retried.nextFollowup())
                .as("실패한 턴이 완료 턴 수에 잡히면 마지막 턴으로 판정되어 꼬리질문이 사라진다")
                .isNotNull();
        assertThat(retried.nextFollowup().question()).isEqualTo("꼬리질문2");
        assertThat(promptText(capturedPrompts(3).get(2)))
                .as("실패한 턴이 남아 있으면 같은 답변이 이력에 중복으로 실린다")
                .containsOnlyOnce("답변2");
    }

    @Test
    @DisplayName("대화 식별자가 다르면 서로의 문답이 이력에 섞이지 않는다.")
    void gradeTwoConversations_doNotShareHistory() {
        // given
        given(chatModel.call(any(Prompt.class))).willReturn(
                aiResponse("피드백A", 9, "꼬리질문A"),
                aiResponse("피드백B", 9, "꼬리질문B"),
                aiResponse("피드백A2", 9, "꼬리질문A2")
        );
        String conversationA = essayAnswerService.startSession(essay.getId()).conversationId();
        String conversationB = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult firstOfA = evaluate(conversationA, essay.getContent(), "A의 답변1");
        evaluate(conversationB, essay.getContent(), "B의 답변1");
        evaluate(conversationA, firstOfA.nextFollowup().question(), "A의 답변2");

        // then
        List<Prompt> prompts = capturedPrompts(3);
        assertThat(promptText(prompts.get(1)))
                .as("B의 첫 턴에 A의 문답이 실리면 안 된다")
                .contains("B의 답변1")
                .doesNotContain("A의 답변1");
        assertThat(promptText(prompts.get(2)))
                .as("A의 두 번째 턴에는 A의 이력만 실려야 한다")
                .contains("A의 답변1", "A의 답변2")
                .doesNotContain("B의 답변1");
    }

    private EssayAnswerResult evaluate(String conversationId, String question, String answer) {
        return essayAnswerService.evaluate(
                10L,
                essay.getId(),
                new EvaluateEssayAnswerCommand(conversationId, question, answer)
        );
    }

    private List<Prompt> capturedPrompts(int expectedCallCount) {
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(expectedCallCount)).call(captor.capture());
        return captor.getAllValues();
    }

    // 대화 이력을 포함해 모델에게 실제로 전달된 전체 텍스트
    private static String promptText(Prompt prompt) {
        return prompt.getInstructions().stream()
                .map(Message::getText)
                .collect(Collectors.joining("\n"));
    }

    // 이번 턴에 새로 붙인 사용자 메시지(이력을 제외한 마지막 메시지)
    private static String currentUserText(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        return messages.get(messages.size() - 1).getText();
    }

    private static ChatResponse aiResponse(String feedback, int score, String followupQuestion) {
        String followupJson = followupQuestion == null ? "null" : "\"%s\"".formatted(followupQuestion);
        String json = """
                {"feedback":"%s","modelAnswer":"모범답안","score":%d,"followupQuestion":%s}
                """.formatted(feedback, score, followupJson);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
    }
}
