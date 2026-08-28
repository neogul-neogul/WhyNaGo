package com.neogul.whynago.question.infra.ai.compare;

import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV1;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV2;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV3;

/**
 * -Dai.compare.promptVersion 으로 고른 프롬프트 버전을 실제 구현으로 바꾼다.
 */
public class EssayPromptVersions {

    public static EssayPrompt of(String version) {
        return switch (version) {
            case "v1" -> new EssayPromptV1();
            case "v2" -> new EssayPromptV2();
            case "v3" -> new EssayPromptV3();
            default -> throw new IllegalArgumentException("알 수 없는 프롬프트 버전이다: " + version);
        };
    }
}
