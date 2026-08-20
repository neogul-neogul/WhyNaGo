package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.util.List;

// 한 번의 평가가 비교하는 두 프롬프트와 그 둘의 핵심 차이다.
//
// 세 개 이상을 한 리포트에 담으면 어떤 변경이 어떤 점수 차이를 만들었는지 귀속시킬 수 없다.
// 항상 기준선 하나와 후보 하나만 두고, 그 둘이 무엇이 다른지를 difference에 사람이 직접 적는다.
// difference는 리포트 맨 위에 실리고 심사 모델에도 그대로 전달돼 채점 기준이 된다.
record PromptComparison(EssayPrompt baseline, EssayPrompt candidate, String difference) {

    List<EssayPrompt> prompts() {
        return List.of(baseline, candidate);
    }

    String baselineVersion() {
        return baseline.version();
    }

    String candidateVersion() {
        return candidate.version();
    }
}
