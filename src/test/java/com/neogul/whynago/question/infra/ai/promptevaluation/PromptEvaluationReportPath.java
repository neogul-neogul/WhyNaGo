package com.neogul.whynago.question.infra.ai.promptevaluation;

import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

final class PromptEvaluationReportPath {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss");

    private PromptEvaluationReportPath() {
    }

    static Path of(Path directory, PromptComparison comparison, Clock clock) {
        String filename = "eval_%s_%s_%s.md".formatted(
                comparison.baselineVersion(),
                comparison.candidateVersion(),
                FILE_TIMESTAMP.format(ZonedDateTime.now(clock)));
        return directory.resolve(filename);
    }
}
