package com.neogul.whynago.question.infra.ai.promptevaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV7;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV8;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PromptEvaluationReportWriterTest {

    private static final String DIFFERENCE = "후보는 꼬리질문을 루브릭 판정과 숙련도에 맞춘다.";
    private static final PromptComparison COMPARISON =
            new PromptComparison(new EssayPromptV7(), new EssayPromptV8(), DIFFERENCE);

    @Test
    @DisplayName("리포트 맨 위에 비교 대상과 두 프롬프트의 핵심 차이를 적는다.")
    void render_missingDifferenceHeader() {
        String markdown = render(List.of(run(new EssayPromptV7(), 4)));

        assertThat(markdown.indexOf(DIFFERENCE)).isLessThan(markdown.indexOf("## 요약"));
        assertThat(markdown)
                .contains("## 비교 대상: `v7`(기준선) vs `v8`(후보)")
                .contains("### 두 프롬프트의 핵심 차이")
                .contains("심사 프롬프트: `v2`");
    }

    @Test
    @DisplayName("심사 표에 차이 반영 점수를 담고 기준선 대비 총점 차이를 계산한다.")
    void render_missingDifferenceImpact() {
        String markdown = render(List.of(run(new EssayPromptV7(), 2), run(new EssayPromptV8(), 5)));

        assertThat(markdown)
                .contains("| 차이 반영 | 총점 | v7 대비 |")
                .contains("| sample-case | `v7`")
                .contains("| 차이 반영 | 2 | 5 |");
        // v7이 기준선이므로 +0, v8은 differenceImpact가 3점 높은 만큼 +3이다.
        assertThat(markdown).contains("| 32 | +0 |").contains("| 35 | +3 |");
    }

    @Test
    @DisplayName("여러 줄 응답과 표 구분자를 마크다운 셀에 넣을 수 있게 escape한다.")
    void render_unescapedCell() {
        String markdown = render(List.of(run(new EssayPromptV7(), 4)));

        assertThat(markdown)
                .contains("#### 평가 입력")
                .contains("| 질문 | 질문 |")
                .contains("| feedback | 피드백\\|첫 줄<br>둘째 줄 |")
                .contains("<pre><code class=\"language-json\">[ ]</code></pre>")
                .contains("근거\\|설명");
    }

    private String render(List<PromptEvaluationRun> runs) {
        return new PromptEvaluationReportWriter(
                Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneId.of("Asia/Seoul")), COMPARISON)
                .render(runs);
    }

    private PromptEvaluationRun run(EssayPrompt prompt, int differenceImpact) {
        return new PromptEvaluationRun(
                new PromptEvaluationCase(
                        "sample-case", null, true, "질문", "사용자 답변", List.of(), null, null, null, 0, null),
                prompt,
                new GradeAndFollowupResult("피드백|첫 줄\n둘째 줄", "모범답안", 7, "꼬리질문", MasteryLevel.SOLID, "근거", List.of()),
                new PromptEvaluationJudge.JudgeResult(5, 4, 3, 4, 5, 4, 5, differenceImpact, "근거|설명"),
                List.of());
    }
}
