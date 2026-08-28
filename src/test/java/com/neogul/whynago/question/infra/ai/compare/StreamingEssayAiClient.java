package com.neogul.whynago.question.infra.ai.compare;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;

/**
 * 응답을 Flux로 받아 도착하는 조각을 그대로 콘솔에 흘리는 비교 테스트용 클라이언트.
 * 운영의 GeminiEssayAiClient는 응답이 다 올 때까지 기다렸다가 한 번에 객체로 바꾸는데,
 * 프롬프트를 고쳐 가며 확인할 때는 모델이 무엇을 써 내려가는지 실시간으로 보는 편이 빠르다.
 *
 * <p>운영과 같은 프롬프트를 쓰기 위해 구조화 출력 지시(BeanOutputConverter의 format)를 직접 덧붙인다.
 * 그래서 흘러나오는 조각은 사람이 읽는 문장이 아니라 JSON 조각이고, 전부 모인 뒤 객체로 변환한다.
 */
@Slf4j
public class StreamingEssayAiClient implements EssayAiClient {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final EssayPrompt essayPrompt;
    private final BeanOutputConverter<GradeAndFollowupResult> converter =
            new BeanOutputConverter<>(GradeAndFollowupResult.class);

    public StreamingEssayAiClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, EssayPrompt essayPrompt) {
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
        String operation = generateFollowup ? "채점·꼬리질문 생성" : "채점";
        String userText = essayPrompt.userPrompt(mode, question, answer, generateFollowup)
                + System.lineSeparator() + converter.getFormat();
        StringBuilder streamed = new StringBuilder();
        long startedAt = System.nanoTime();

        System.out.printf("%n--- 스트리밍 시작 (%s) ---%n", operation);
        List<ChatResponse> responses = chatClient.prompt()
                .system(essayPrompt.systemPrompt(mode))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userText)
                .stream()
                .chatResponse()
                .doOnNext(response -> printChunk(response, streamed))
                .collectList()
                .block();
        System.out.printf("%n--- 스트리밍 끝 ---%n");

        logCompletion(operation, conversationId, responses, startedAt);
        return convert(streamed.toString());
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

    // 조각이 도착하는 즉시 줄바꿈 없이 이어 붙여 찍는다. 버퍼에 남지 않게 바로 flush 한다.
    private void printChunk(ChatResponse response, StringBuilder streamed) {
        String chunk = response.getResult() == null ? null : response.getResult().getOutput().getText();
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        streamed.append(chunk);
        System.out.print(chunk);
        System.out.flush();
    }

    private GradeAndFollowupResult convert(String streamed) {
        if (streamed.isBlank()) {
            throw new IllegalStateException("스트리밍으로 받은 응답이 비어 있다.");
        }
        try {
            return converter.convert(streamed);
        } catch (RuntimeException e) {
            throw new IllegalStateException("스트리밍 응답을 결과 객체로 바꾸지 못했다: " + streamed, e);
        }
    }

    // 비교 리포트가 읽어 가는 로그라 운영 클라이언트와 같은 형식으로 남긴다.
    private void logCompletion(String operation, String conversationId, List<ChatResponse> responses, long startedAt) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        ChatResponse last = responses.getLast();
        Usage usage = usage(responses);
        if (usage == null) {
            log.info("스트리밍 {} 완료 - conversationId={}, model={}, promptVersion={}, elapsedMs={}",
                    operation, conversationId, last.getMetadata().getModel(), essayPrompt.version(),
                    elapsedMillis(startedAt));
            return;
        }
        log.info("스트리밍 {} 완료 - conversationId={}, model={}, promptVersion={}, promptTokens={}, "
                        + "completionTokens={}, totalTokens={}, elapsedMs={}",
                operation,
                conversationId,
                last.getMetadata().getModel(),
                essayPrompt.version(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens(),
                elapsedMillis(startedAt));
    }

    // 토큰 사용량은 마지막 조각에만 실려 오는데, 그 뒤에 사용량이 0인 조각이 더 붙기도 한다.
    // 사용량을 아예 주지 않는 모델도 있어, 값이 실린 마지막 조각만 골라 쓰고 없으면 토큰을 남기지 않는다.
    private Usage usage(List<ChatResponse> responses) {
        return responses.stream()
                .map(response -> response.getMetadata().getUsage())
                .filter(usage -> usage != null && usage.getTotalTokens() != null && usage.getTotalTokens() > 0)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
