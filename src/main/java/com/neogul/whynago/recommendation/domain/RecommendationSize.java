package com.neogul.whynago.recommendation.domain;

// 한 번의 추천 응답에 담는 문항 수다. 콜드스타트·맞춤 추천이 같은 수를 내려야 하므로 한 곳에 둔다.
// 문항 하나가 채점까지 합쳐 최대 4회의 AI 호출을 유발하므로 문서상 상한(6문항)보다 보수적으로 잡는다.
public final class RecommendationSize {

    public static final int TARGET_QUESTION_COUNT = 3;

    private RecommendationSize() {
    }
}
