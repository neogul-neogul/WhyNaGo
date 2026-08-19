package com.neogul.whynago.recommendation.infra.ai;

import com.neogul.whynago.recommendation.domain.GeneratedEssay;
import com.neogul.whynago.recommendation.infra.ai.dto.EssayGenerationRequest;
import java.util.List;

// API 키가 없는 로컬·테스트 환경에서 파이프라인을 끝까지 돌려보기 위한 대체 구현이다.
// 검증기를 통과하는 최소 형태를 요청 그대로 되돌려준다.
public class MockEssayQuestionAiClient implements EssayQuestionAiClient {

    private static final String TITLE_PREFIX = "[MOCK] ";

    @Override
    public GeneratedEssay generate(EssayGenerationRequest request) {
        return new GeneratedEssay(
                TITLE_PREFIX + request.category().name() + " 서술형 문항",
                TITLE_PREFIX + request.category().name()
                        + " 주제에서 취약한 개념을 설명하고, 실무에서 어떤 문제로 드러나는지 서술하라.",
                TITLE_PREFIX + "로컬 임시 모범답안이다. 개념 정의와 동작 원리, 실무에서의 영향을 순서대로 설명한다.",
                List.of(TITLE_PREFIX + "개념 정의", TITLE_PREFIX + "실무 영향"),
                request.category(),
                request.targetDifficulty(),
                mockTags(request)
        );
    }

    // 요청한 취약 태그가 있으면 그 태그를, 없으면 카테고리 사전의 첫 태그를 쓴다.
    private List<String> mockTags(EssayGenerationRequest request) {
        if (!request.weakTags().isEmpty()) {
            return List.of(request.weakTags().get(0));
        }
        if (!request.allowedTags().isEmpty()) {
            return List.of(request.allowedTags().get(0));
        }
        return List.of();
    }
}
