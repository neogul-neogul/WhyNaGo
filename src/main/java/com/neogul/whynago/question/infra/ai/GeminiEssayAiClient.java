package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.common.ai.AiFailureClassifier;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.exception.ErrorCode;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class GeminiEssayAiClient implements EssayAiClient {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final EssayPrompt essayPrompt;

    public GeminiEssayAiClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, EssayPrompt essayPrompt) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatMemory = chatMemory;
        this.essayPrompt = essayPrompt;
    }

    @Override
    public GradeAndFollowupResult gradeAndGenerateFollowup(
            String conversationId,
            String question,
            String answer,
            boolean generateFollowup,
            EssayGradingMode mode
    ) {
        String userText = essayPrompt.userPrompt(mode, question, answer, generateFollowup);

        return call(generateFollowup, conversationId, () -> chatClient.prompt()
                .system(essayPrompt.systemPrompt(mode))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userText)
                .call()
                .responseEntity(GradeAndFollowupResult.class));
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
    private GradeAndFollowupResult call(
            boolean generateFollowup,
            String conversationId,
            Supplier<ResponseEntity<ChatResponse, GradeAndFollowupResult>> aiCall
    ) {
        String operation = generateFollowup ? "채점·꼬리질문 생성" : "채점";
        long startedAt = System.nanoTime();
        List<Message> beforeCall = List.copyOf(chatMemory.get(conversationId));
        try {
            ResponseEntity<ChatResponse, GradeAndFollowupResult> result = aiCall.get();
            logCompletion(operation, conversationId, result.response(), startedAt);
            return result.entity();
        } catch (RuntimeException e) {
            ErrorCode errorCode = errorCodeOf(e);
            log.warn("Gemini {} 실패 - conversationId={}, errorCode={}, elapsedMs={}, cause={}",
                    operation, conversationId, errorCode.code(), elapsedMillis(startedAt), e.toString());
            rollbackMemory(conversationId, beforeCall);
            throw new BusinessException(errorCode, e);
        }
    }

    private void logCompletion(String operation, String conversationId, ChatResponse response, long startedAt) {
        Usage usage = response.getMetadata().getUsage();
        log.info("Gemini {} 완료 - conversationId={}, model={}, promptVersion={}, promptTokens={}, completionTokens={}, "
                        + "totalTokens={}, elapsedMs={}",
                operation,
                conversationId,
                response.getMetadata().getModel(),
                essayPrompt.version(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens(),
                elapsedMillis(startedAt));
    }

    private void rollbackMemory(String conversationId, List<Message> beforeCall) {
        chatMemory.clear(conversationId);
        if (!beforeCall.isEmpty()) {
            chatMemory.add(conversationId, beforeCall);
        }
    }

    // 채점 쿼터 버킷의 에러코드로 옮긴다. 추천 문제 생성은 같은 분류를 쓰지만 다른 버킷으로 옮긴다.
    private ErrorCode errorCodeOf(RuntimeException e) {
        return switch (AiFailureClassifier.classify(e)) {
            case DAILY_QUOTA_EXCEEDED -> QuestionErrorCode.ESSAY_AI_DAILY_QUOTA_EXCEEDED;
            case QUOTA_EXCEEDED -> QuestionErrorCode.ESSAY_AI_QUOTA_EXCEEDED;
            case UNAVAILABLE -> QuestionErrorCode.ESSAY_AI_UNAVAILABLE;
        };
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
