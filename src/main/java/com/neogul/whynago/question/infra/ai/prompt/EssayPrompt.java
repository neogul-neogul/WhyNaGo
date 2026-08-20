package com.neogul.whynago.question.infra.ai.prompt;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;

public interface EssayPrompt {

    String version();

    String systemPrompt(EssayGradingMode mode);

    // target의 rubric은 null일 수 있고 solvingTime은 미측정일 수 있다. 이전 버전 프롬프트는 그 둘을
    // 쓰지 않는다 - 판정 품질이 떨어질 때 되돌릴 수 있게 남겨 둔 클래스들이다.
    String userPrompt(EssayGradingMode mode, EssayGradingTarget target, boolean generateFollowup);
}
