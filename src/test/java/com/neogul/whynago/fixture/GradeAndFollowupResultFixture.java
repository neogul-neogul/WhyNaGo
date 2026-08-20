package com.neogul.whynago.fixture;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.infra.ai.CriterionGrading;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import java.util.List;

// 대부분의 테스트는 숙련도 값 자체에 관심이 없어 기본값으로 채운다.
// 숙련도가 검증 의도에 중요한 테스트만 withMastery / withoutMastery를 쓴다.
// 루브릭 항목 판정도 같은 이유로 기본은 빈 목록이고, 필요한 테스트만 withCriteria를 쓴다.
public final class GradeAndFollowupResultFixture {

    private static final String MASTERY_REASON = "핵심 개념은 짚었지만 근거가 한 단계 빠졌다.";

    private GradeAndFollowupResultFixture() {
    }

    public static GradeAndFollowupResult of(String feedback, String modelAnswer, int score, String followupQuestion) {
        return withMastery(feedback, modelAnswer, score, followupQuestion, MasteryLevel.SOLID);
    }

    public static GradeAndFollowupResult withMastery(
            String feedback,
            String modelAnswer,
            int score,
            String followupQuestion,
            MasteryLevel mastery
    ) {
        return new GradeAndFollowupResult(
                feedback, modelAnswer, score, followupQuestion, mastery, MASTERY_REASON, List.of());
    }

    // AI가 숙련도를 판정하지 못한 응답이다.
    public static GradeAndFollowupResult withoutMastery(
            String feedback,
            String modelAnswer,
            int score,
            String followupQuestion
    ) {
        return new GradeAndFollowupResult(feedback, modelAnswer, score, followupQuestion, null, null, List.of());
    }

    // 루브릭 항목별 판정까지 담은 응답이다. score는 서버가 배점 합으로 덮어쓰므로 여기 값은 폴백용이다.
    public static GradeAndFollowupResult withCriteria(int score, List<CriterionGrading> criteriaResults) {
        return new GradeAndFollowupResult(
                "피드백", "모범답안", score, null, MasteryLevel.SOLID, MASTERY_REASON, criteriaResults);
    }
}
