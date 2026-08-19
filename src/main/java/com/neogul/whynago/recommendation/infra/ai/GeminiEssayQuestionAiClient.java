package com.neogul.whynago.recommendation.infra.ai;

import com.neogul.whynago.common.ai.AiFailureClassifier;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.exception.ErrorCode;
import com.neogul.whynago.recommendation.domain.GeneratedEssay;
import com.neogul.whynago.recommendation.exception.RecommendationErrorCode;
import com.neogul.whynago.recommendation.infra.ai.dto.EssayGenerationRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;

@Slf4j
public class GeminiEssayQuestionAiClient implements EssayQuestionAiClient {

    private static final String SYSTEM_PROMPT = """
            당신은 개발자 채용 면접관이다. 지원자의 취약한 주제를 파고드는 서술형 면접 문항을 만든다.
            사실과 다른 내용을 모범답안에 쓰지 않는다. 확신이 없으면 더 기본적인 개념으로 범위를 좁힌다.
            """;
    // 채점 대화와 달리 대화 이력을 쓰지 않는다. 생성은 매번 독립 요청이다.
    private final ChatClient chatClient;
    private final Resource promptTemplate;

    public GeminiEssayQuestionAiClient(ChatClient.Builder chatClientBuilder, Resource promptTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplate = promptTemplate;
    }

    @Override
    public GeneratedEssay generate(EssayGenerationRequest request) {
        String userText = new PromptTemplate(promptTemplate).render(variables(request));
        long startedAt = System.nanoTime();
        try {
            GeneratedEssay generated = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userText)
                    .call()
                    .entity(GeneratedEssay.class);
            log.info("맞춤 서술형 생성 완료 - category={}, difficulty={}, elapsedMs={}",
                    request.category(), request.targetDifficulty(), elapsedMillis(startedAt));
            return generated;
        } catch (RuntimeException e) {
            ErrorCode errorCode = errorCodeOf(e);
            log.warn("맞춤 서술형 생성 실패 - category={}, errorCode={}, elapsedMs={}, cause={}",
                    request.category(), errorCode.code(), elapsedMillis(startedAt), e.toString());
            throw new BusinessException(errorCode, e);
        }
    }

    private Map<String, Object> variables(EssayGenerationRequest request) {
        return Map.of(
                "category", request.category().name(),
                "weakTags", joinOrNone(request.weakTags()),
                "allowedTags", joinOrNone(request.allowedTags()),
                "targetDifficulty", request.targetDifficulty().name(),
                "weaknessScore", String.format("%.2f", request.weaknessScore()),
                "reason", request.reason(),
                "wrongExplanations", bulletsOrNone(request.wrongExplanations()),
                "existingTitles", bulletsOrNone(request.existingTitles())
        );
    }

    private String joinOrNone(List<String> values) {
        return values.isEmpty() ? "없음" : String.join(", ", values);
    }

    private String bulletsOrNone(List<String> values) {
        if (values.isEmpty()) {
            return "없음";
        }
        return values.stream().map(value -> "- " + value).reduce((a, b) -> a + "\n" + b).orElse("없음");
    }

    // 추천 전용 쿼터 버킷으로 옮긴다. 채점 쿼터(ESSAY_AI_*)를 쓰지 않는 것이 이 매핑의 목적이다.
    private ErrorCode errorCodeOf(RuntimeException e) {
        return switch (AiFailureClassifier.classify(e)) {
            case DAILY_QUOTA_EXCEEDED -> RecommendationErrorCode.RECOMMENDATION_AI_DAILY_QUOTA_EXCEEDED;
            case QUOTA_EXCEEDED -> RecommendationErrorCode.RECOMMENDATION_AI_QUOTA_EXCEEDED;
            case UNAVAILABLE -> RecommendationErrorCode.RECOMMENDATION_AI_UNAVAILABLE;
        };
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
