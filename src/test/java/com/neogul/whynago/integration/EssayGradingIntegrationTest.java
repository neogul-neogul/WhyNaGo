package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionStatRepository;
import com.neogul.whynago.question.service.EssayAnswerService;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.EssaySessionResult;
import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import com.neogul.whynago.question.service.dto.RubricCriterionResult;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
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
    private QuestionStatRepository questionStatRepository;

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

    @Test
    @DisplayName("루브릭이 있는 문항은 채점 기준을 프롬프트에 내려 항목별 판정을 받고 배점 합을 점수로 쓴다.")
    void gradeWithRubric() {
        // given
        essay = questionRepository.save(QuestionFixture.essayRootWithRubric());
        given(chatModel.call(any(Prompt.class))).willReturn(rubricAiResponse(10, true, false, true));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult result = evaluate(conversationId, essay.getContent(), "답변1");

        // then
        assertThat(currentUserText(capturedPrompts(1).get(0)))
                .as("루브릭 항목이 배점과 번호와 함께 프롬프트에 실려야 한다")
                .contains("[채점 기준]")
                .contains("1. (배점 3) TCP는 신뢰성 있는 데이터 전송이 필요한 경우에 사용된다.")
                .contains("3. (배점 4) TCP의 흐름 제어와 혼잡 제어가 처리 지연을 유발한다.")
                .contains("꼬리질문은 다음 개념 범위 안에서 물어라: 흐름 제어, 혼잡 제어");
        assertThat(result.grading().score())
                .as("AI가 적은 10점이 아니라 충족 항목(3+4)의 배점 합이어야 한다")
                .isEqualTo(7);
        assertThat(result.grading().isCorrect()).isTrue();
        assertThat(result.grading().rubricCriteria())
                .extracting(RubricCriterionResult::weight, RubricCriterionResult::met,
                        RubricCriterionResult::reason)
                .containsExactly(
                        tuple(3, true, "근거1"),
                        tuple(3, false, "근거2"),
                        tuple(4, true, "근거3"));
    }

    @Test
    @DisplayName("꼬리질문 턴에는 루브릭을 내려보내지 않고 항목 판정도 비운다.")
    void gradeWithRubric_followupTurn() {
        // given
        essay = questionRepository.save(QuestionFixture.essayRootWithRubric());
        given(chatModel.call(any(Prompt.class))).willReturn(
                rubricAiResponse(0, true, true, true),
                aiResponse("피드백2", 8, "꼬리질문2"));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();
        EssayAnswerResult first = evaluate(conversationId, essay.getContent(), "답변1");

        // when
        EssayAnswerResult second = evaluate(conversationId, first.nextFollowup().question(), "답변2");

        // then
        assertThat(currentUserText(capturedPrompts(2).get(1)))
                .doesNotContain("[채점 기준]")
                .contains("criteriaResults는 빈 배열로 두어라");
        assertThat(second.grading().rubricCriteria()).isEmpty();
        assertThat(second.grading().score())
                .as("루브릭이 없는 턴은 AI 점수를 그대로 쓴다")
                .isEqualTo(8);
    }

    @Test
    @DisplayName("소요시간과 문항 평균을 프롬프트에 실어 내리고 평균보다 오래 걸린 답변은 점수를 1점 깎는다.")
    void gradeWithSlowSolvingTime() {
        // given
        questionStatRepository.save(QuestionStat.of(essay.getId(), 100, 0.5, 20, LocalDateTime.now()));
        given(chatModel.call(any(Prompt.class))).willReturn(aiResponse("피드백1", 8, "꼬리질문1"));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult result = evaluate(conversationId, essay.getContent(), "답변1", 300);

        // then
        assertThat(currentUserText(capturedPrompts(1).get(0)))
                .contains("[소요시간]")
                .contains("이 답변을 쓰는 데 300초가 걸렸다. 이 문항의 평균 소요시간은 100초이며, 평균 대비 뚜렷하게 오래 걸렸다.");
        assertThat(result.grading().score())
                .as("AI가 매긴 8점에서 느린 시간으로 1점을 깎는다")
                .isEqualTo(7);
        assertThat(result.grading().solvingTime().scoreAdjustment()).isEqualTo(-1);
        assertThat(result.grading().solvingTime().pace()).isEqualTo(ElapsedPace.SLOW);
    }

    @Test
    @DisplayName("평균보다 빠른 답변은 점수를 1점 올려 통과 여부까지 바꾼다.")
    void gradeWithFastSolvingTime() {
        // given
        questionStatRepository.save(QuestionStat.of(essay.getId(), 300, 0.5, 20, LocalDateTime.now()));
        given(chatModel.call(any(Prompt.class))).willReturn(aiResponse("피드백1", 6, "꼬리질문1"));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult result = evaluate(conversationId, essay.getContent(), "답변1", 100);

        // then
        assertThat(result.grading().score()).isEqualTo(7);
        assertThat(result.grading().isCorrect()).isTrue();
        assertThat(result.grading().solvingTime().pace()).isEqualTo(ElapsedPace.FAST);
    }

    @Test
    @DisplayName("소요시간을 보내지 않으면 시간을 프롬프트에 넣지 않고 점수도 조정하지 않는다.")
    void gradeWithoutSolvingTime() {
        // given
        questionStatRepository.save(QuestionStat.of(essay.getId(), 100, 0.5, 20, LocalDateTime.now()));
        given(chatModel.call(any(Prompt.class))).willReturn(aiResponse("피드백1", 8, "꼬리질문1"));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult result = evaluate(conversationId, essay.getContent(), "답변1");

        // then
        assertThat(currentUserText(capturedPrompts(1).get(0)))
                .doesNotContain("[소요시간]")
                .contains("소요시간은 측정되지 않았다");
        assertThat(result.grading().score()).isEqualTo(8);
        assertThat(result.grading().solvingTime()).isNull();
    }

    @Test
    @DisplayName("루브릭 배점 합에 소요시간 가감을 얹어 최종 점수를 낸다.")
    void gradeWithRubricAndSolvingTime() {
        // given
        essay = questionRepository.save(QuestionFixture.essayRootWithRubric());
        questionStatRepository.save(QuestionStat.of(essay.getId(), 100, 0.5, 20, LocalDateTime.now()));
        given(chatModel.call(any(Prompt.class))).willReturn(rubricAiResponse(0, true, true, true));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult result = evaluate(conversationId, essay.getContent(), "답변1", 300);

        // then
        assertThat(result.grading().rubricCriteria())
                .as("전 항목을 충족했으므로 루브릭 점수는 10점이다")
                .allMatch(RubricCriterionResult::met);
        assertThat(result.grading().score())
                .as("루브릭 10점에서 느린 시간으로 1점을 깎는다")
                .isEqualTo(9);
    }

    @Test
    @DisplayName("항목 판정 개수가 루브릭과 맞지 않으면 채점을 실패시키지 않고 AI 점수로 폴백한다.")
    void gradeWithRubric_mismatchedCriteria() {
        // given
        essay = questionRepository.save(QuestionFixture.essayRootWithRubric());
        given(chatModel.call(any(Prompt.class))).willReturn(rubricAiResponse(5, true));
        String conversationId = essayAnswerService.startSession(essay.getId()).conversationId();

        // when
        EssayAnswerResult result = evaluate(conversationId, essay.getContent(), "답변1");

        // then
        assertThat(result.grading().score()).isEqualTo(5);
        assertThat(result.grading().rubricCriteria()).isEmpty();
    }

    private EssayAnswerResult evaluate(String conversationId, String question, String answer) {
        return evaluate(conversationId, question, answer, null);
    }

    private EssayAnswerResult evaluate(
            String conversationId, String question, String answer, Integer elapsedSeconds) {
        return essayAnswerService.evaluate(
                10L,
                essay.getId(),
                new EvaluateEssayAnswerCommand(conversationId, question, answer, elapsedSeconds)
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

    // criteriaResults까지 담은 응답. score는 서버가 배점 합으로 덮어쓰므로 일부러 어긋나게 준다.
    private static ChatResponse rubricAiResponse(int score, boolean... met) {
        StringBuilder criteria = new StringBuilder();
        for (int index = 0; index < met.length; index++) {
            criteria.append(index == 0 ? "" : ",")
                    .append("{\"index\":%d,\"met\":%b,\"reason\":\"근거%d\"}"
                            .formatted(index + 1, met[index], index + 1));
        }
        String json = """
                {"feedback":"피드백","modelAnswer":"모범답안","score":%d,"followupQuestion":"꼬리질문1",\
                "criteriaResults":[%s]}
                """.formatted(score, criteria);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
    }

    private static ChatResponse aiResponse(String feedback, int score, String followupQuestion) {
        String followupJson = followupQuestion == null ? "null" : "\"%s\"".formatted(followupQuestion);
        String json = """
                {"feedback":"%s","modelAnswer":"모범답안","score":%d,"followupQuestion":%s}
                """.formatted(feedback, score, followupJson);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
    }
}
