package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.recommendation.domain.GeneratedEssay;
import com.neogul.whynago.recommendation.domain.GeneratedEssayValidator;
import com.neogul.whynago.recommendation.domain.GenerationTopic;
import com.neogul.whynago.recommendation.infra.ai.EssayQuestionAiClient;
import com.neogul.whynago.recommendation.infra.ai.dto.EssayGenerationRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 취약 주제 1개를 AI 서술형 문항 1개로 바꾼다. 검증을 통과하지 못한 결과는 재시도하지 않고 버린다.
@Slf4j
@Component
@RequiredArgsConstructor
public class EssayQuestionGenerator {

    // 오답 해설을 너무 많이 넣으면 프롬프트가 오개념으로 뒤덮여 생성 품질이 떨어진다.
    private static final int MAX_WRONG_EXPLANATIONS = 5;

    private final EssayQuestionAiClient essayQuestionAiClient;
    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;
    private final GeneratedEssayValidator generatedEssayValidator;

    public Optional<GeneratedEssay> generate(GenerationTopic topic) {
        List<String> allowedTags = questionReader.readTagDictionary(topic.category());
        EssayGenerationRequest request = new EssayGenerationRequest(
                topic.category(),
                topic.tags(),
                allowedTags,
                topic.targetDifficulty(),
                topic.weaknessScore(),
                topic.reason(),
                answerChoiceReader.readWrongExplanations(topic.category(), topic.tags(), MAX_WRONG_EXPLANATIONS),
                questionReader.readEssayTitles(topic.category(), topic.tags())
        );

        GeneratedEssay candidate = essayQuestionAiClient.generate(request);

        List<String> violations = generatedEssayValidator.validate(
                candidate,
                topic,
                Set.copyOf(allowedTags),
                Set.copyOf(request.existingTitles())
        );
        if (!violations.isEmpty()) {
            log.warn("생성 문항 검증 실패로 버림 - category={}, tags={}, violations={}",
                    topic.category(), topic.tags(), violations);
            return Optional.empty();
        }
        return Optional.of(candidate);
    }
}
