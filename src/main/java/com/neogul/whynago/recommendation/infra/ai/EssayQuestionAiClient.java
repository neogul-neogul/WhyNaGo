package com.neogul.whynago.recommendation.infra.ai;

import com.neogul.whynago.recommendation.domain.GeneratedEssay;
import com.neogul.whynago.recommendation.infra.ai.dto.EssayGenerationRequest;

public interface EssayQuestionAiClient {

    // 요청 1건당 문항 1개를 만든다. 여러 개를 한 번에 받으면 실패 지점이 뭉쳐 부분 성공을 살릴 수 없다.
    GeneratedEssay generate(EssayGenerationRequest request);
}
