package com.neogul.whynago.recommendation.service;

import static com.neogul.whynago.recommendation.domain.RecommendationSize.TARGET_QUESTION_COUNT;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import com.neogul.whynago.recommendation.implement.ColdStartQuestionReader;
import com.neogul.whynago.recommendation.implement.PersonalizedQuestionProvider;
import com.neogul.whynago.recommendation.implement.RecommendedQuestionAssembler;
import com.neogul.whynago.recommendation.implement.WeakTagAssembler;
import com.neogul.whynago.recommendation.implement.WeaknessProfileCalculator;
import com.neogul.whynago.recommendation.service.dto.RecommendationResult;
import com.neogul.whynago.recommendation.service.dto.WeakTagsResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final WeaknessProfileCalculator weaknessProfileCalculator;
    private final WeakTagAssembler weakTagAssembler;
    private final ColdStartQuestionReader coldStartQuestionReader;
    private final PersonalizedQuestionProvider personalizedQuestionProvider;
    private final RecommendedQuestionAssembler recommendedQuestionAssembler;

    /** 전체 풀이 이력에서 계산한 약점도 상위 태그를 추천 화면에 제공한다. */
    @Transactional(readOnly = true)
    public WeakTagsResult weakTags(Long userId) {
        WeaknessProfile profile = weaknessProfileCalculator.calculate(userId);

        return weakTagAssembler.assemble(profile);
    }

    @Transactional
    public RecommendationResult recommend(Long userId) {
        WeaknessProfile profile = weaknessProfileCalculator.calculate(userId);
        if (profile.isColdStart()) {
            return coldStart();
        }

        return personalized(personalizedQuestionProvider.provide(userId, profile));
    }

    // 약점을 진단할 만큼 풀지 않은 사용자다. AI를 호출하지 않는다.
    private RecommendationResult coldStart() {
        return RecommendationResult.of(
                recommendedQuestionAssembler.assemble(coldStartQuestionReader.read(TARGET_QUESTION_COUNT)),
                false
        );
    }

    private RecommendationResult personalized(List<Question> questions) {
        return RecommendationResult.of(recommendedQuestionAssembler.assemble(questions), true);
    }
}
