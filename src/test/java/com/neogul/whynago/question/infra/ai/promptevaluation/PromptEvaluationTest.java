package com.neogul.whynago.question.infra.ai.promptevaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neogul.whynago.WhynagoApplication;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("prompt-evaluation")
@SpringBootTest(classes = WhynagoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PromptEvaluationTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void comparesBaselineAndCandidatePromptAndWritesMarkdownReport() {
        // 어떤 쌍을 비교할지는 -Pcomparison=v6-v8 로 고른다. 기본값은 직전 버전과의 비교다.
        String comparisonName = System.getProperty("prompt.evaluation.comparison",
                PromptEvaluationCatalog.DEFAULT_COMPARISON);
        PromptComparison comparison = PromptEvaluationCatalog.comparison(comparisonName);
        PromptEvaluationCatalog.validate(comparison);
        List<EssayPrompt> prompts = comparison.prompts();
        List<PromptEvaluationCase> cases = PromptEvaluationCases.load(objectMapper);
        PromptEvaluationJudge judge =
                new PromptEvaluationJudge(chatClientBuilder, objectMapper, comparison.difference());
        PromptEvaluationRunner runner = new PromptEvaluationRunner(chatClientBuilder, judge);

        List<PromptEvaluationRun> runs = cases.stream()
                .flatMap(testCase -> prompts.stream().map(prompt -> runner.run(testCase, prompt)))
                .toList();

        Clock clock = Clock.systemDefaultZone();
        Path reportDirectory = Path.of(System.getProperty("prompt.evaluation.report.dir", "docs/prompt-evaluations"));
        Path reportPath = PromptEvaluationReportPath.of(reportDirectory, comparison, clock);
        new PromptEvaluationReportWriter(clock, comparison).write(reportPath, runs);

        assertThat(runs).allSatisfy(run -> assertThat(run.contractFailures())
                .as("%s / %s", run.caseId(), run.prompt().version())
                .isEmpty());
    }
}
