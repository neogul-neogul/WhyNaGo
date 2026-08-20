package com.neogul.whynago.question.infra.ai.promptevaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV7;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV8;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PromptEvaluationReportPathTest {

    @Test
    @DisplayName("파일명에 기준선·후보 버전과 실행 시각을 담는다.")
    void of_missingVersionsInFilename() {
        Path path = PromptEvaluationReportPath.of(
                Path.of("docs/prompt-evaluations"),
                new PromptComparison(new EssayPromptV7(), new EssayPromptV8(), "차이"),
                Clock.fixed(Instant.parse("2026-08-20T03:45:06Z"), ZoneId.of("Asia/Seoul")));

        assertThat(path.toString()).isEqualTo("docs/prompt-evaluations/eval_v7_v8_20260820-124506.md");
    }
}
