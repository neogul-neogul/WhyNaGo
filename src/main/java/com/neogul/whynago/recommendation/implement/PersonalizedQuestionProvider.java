package com.neogul.whynago.recommendation.implement;

import static com.neogul.whynago.recommendation.domain.RecommendationSize.TARGET_QUESTION_COUNT;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.recommendation.domain.RecommendationTopicPolicy;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 약점 프로필에 맞춘 문항을 확보한다. 오늘 이미 만들어 둔 것이 있으면 그대로 쓰고,
// 없으면 취약 주제를 골라 생성한 뒤 모자란 자리를 기존 문항으로 채워 캐시에 남긴다.
//
// 생성이 몇 건 성공했는지에 따라 결과가 갈리지만 호출자는 그 사정을 알 필요가 없다.
// 어느 경로로 왔든 "이 사용자에게 줄 맞춤 문항"이라는 결과는 같기 때문이다.
@Component
@RequiredArgsConstructor
public class PersonalizedQuestionProvider {

    private final RecommendationCache recommendationCache;
    private final RecommendationTopicPolicy recommendationTopicPolicy;
    private final GeneratedQuestionCollector generatedQuestionCollector;
    private final FallbackQuestionReader fallbackQuestionReader;

    public List<Question> provide(Long userId, WeaknessProfile profile) {
        List<Question> cached = recommendationCache.find(userId, profile);
        if (!cached.isEmpty()) {
            return cached;
        }

        List<Question> generated = generatedQuestionCollector.collect(
                recommendationTopicPolicy.select(profile, TARGET_QUESTION_COUNT));
        List<Question> questions = fallbackQuestionReader.fill(generated, profile, TARGET_QUESTION_COUNT);
        recommendationCache.put(userId, profile, questions);

        return questions;
    }
}
