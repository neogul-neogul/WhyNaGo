package com.neogul.whynago.question.infra.ai;

import java.util.List;

public interface EssayAiClient {

    // 지금까지의 문답에서 마지막 답변을 채점한다.
    GradedAnswer grade(List<EssayTurn> thread);

    // 지금까지의 문답을 바탕으로 이어질 꼬리질문을 생성한다.
    GeneratedFollowup generateFollowup(List<EssayTurn> thread);
}
