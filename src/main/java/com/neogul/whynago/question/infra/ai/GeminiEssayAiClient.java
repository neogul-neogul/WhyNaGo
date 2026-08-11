package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.exception.ErrorCode;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.retry.NonTransientAiException;

@Slf4j
public class GeminiEssayAiClient implements EssayAiClient {

    private static final String SYSTEM_PROMPT = """
            너는 개발자 채용 기술 면접관이다. 사용자의 답변을 평가한다.
            한국어로 작성하고, 정답을 단정하기보다 보완할 점 중심으로 feedback과 modelAnswer를 채워라.
            score는 0부터 10 사이 정수로, 답변의 정확성과 완성도를 평가해 매겨라.
            이전 문답이 대화 이력으로 주어지면 그 맥락을 활용하되, 항상 마지막에 주어진 '채점 대상' 답변만 평가하라.
            """;

    private static final String GENERATE_FOLLOWUP_INSTRUCTION = """
            또한 이 문답 흐름에 이어서 지원자의 이해도를 더 깊이 확인할 꼬리질문 한 개를 한국어로 생성하라.
            새로운 주제로 벗어나지 말고 직전 답변을 파고들어 followupQuestion에 담아라.""";

    private static final String NO_FOLLOWUP_INSTRUCTION =
            "이번 턴에서는 꼬리질문을 생성하지 말고 followupQuestion은 null로 두어라.";

    private static final String QUOTA_STATUS = "RESOURCE_EXHAUSTED";
    private static final Pattern QUOTA_STATUS_CODE = Pattern.compile("\\b429\\b");
    private static final List<String> DAILY_QUOTA_HINTS = List.of("perday", "per day", "daily");

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public GeminiEssayAiClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatMemory = chatMemory;
    }

    @Override
    public GradeAndFollowupResult gradeAndGenerateFollowup(
            String conversationId,
            String question,
            String answer,
            boolean generateFollowup
    ) {
        String followupInstruction = generateFollowup ? GENERATE_FOLLOWUP_INSTRUCTION : NO_FOLLOWUP_INSTRUCTION;
        String userText = """
                [채점 대상]
                질문: %s
                답변: %s

                %s
                """.formatted(question, answer, followupInstruction);

        return call(generateFollowup, conversationId, () -> chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userText)
                .call()
                .entity(GradeAndFollowupResult.class));
    }

    @Override
    public int completedTurns(String conversationId) {
        return (int) chatMemory.get(conversationId).stream()
                .filter(message -> message.getMessageType() == MessageType.ASSISTANT)
                .count();
    }

    @Override
    public void clearSession(String conversationId) {
        chatMemory.clear(conversationId);
    }

    // 외부 AI 호출 실패는 기술 예외를 노출하지 않고 도메인 에러코드로 변환한다.
    // 원인 예외는 BusinessException에 그대로 담아 전달해, GlobalExceptionHandler가 실제 장애의 stack trace를 로그로 남기게 한다.
    // 이 지점에서는 stack trace 없이 conversationId·에러코드·소요 시간 같은 운영 지표만 한 줄로 남겨 중복 로깅을 피한다.
    private <T> T call(boolean generateFollowup, String conversationId, Supplier<T> aiCall) {
        String operation = generateFollowup ? "채점·꼬리질문 생성" : "채점";
        long startedAt = System.nanoTime();
        List<Message> beforeCall = List.copyOf(chatMemory.get(conversationId));
        try {
            T result = aiCall.get();
            log.info("Gemini {} 완료 - conversationId={}, {}ms",
                    operation, conversationId, elapsedMillis(startedAt));
            return result;
        } catch (RuntimeException e) {
            ErrorCode errorCode = errorCodeOf(e);
            log.warn("Gemini {} 실패 - conversationId={}, errorCode={}, {}ms, cause={}",
                    operation, conversationId, errorCode.code(), elapsedMillis(startedAt), e.toString());
            rollbackMemory(conversationId, beforeCall);
            throw new BusinessException(errorCode, e);
        }
    }

    private void rollbackMemory(String conversationId, List<Message> beforeCall) {
        chatMemory.clear(conversationId);
        if (!beforeCall.isEmpty()) {
            chatMemory.add(conversationId, beforeCall);
        }
    }

    private ErrorCode errorCodeOf(RuntimeException e) {
        String detail = e.getMessage();
        if (!(e instanceof NonTransientAiException) || !isQuotaExceeded(detail)) {
            return QuestionErrorCode.ESSAY_AI_UNAVAILABLE;
        }
        return isDailyQuota(detail)
                ? QuestionErrorCode.ESSAY_AI_DAILY_QUOTA_EXCEEDED
                : QuestionErrorCode.ESSAY_AI_QUOTA_EXCEEDED;
    }

    private boolean isQuotaExceeded(String detail) {
        if (detail == null) {
            return false;
        }
        return detail.contains(QUOTA_STATUS) || QUOTA_STATUS_CODE.matcher(detail).find();
    }

    private boolean isDailyQuota(String detail) {
        String normalized = detail.toLowerCase(Locale.ROOT);
        return DAILY_QUOTA_HINTS.stream().anyMatch(normalized::contains);
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
