package com.neogul.whynago.solvedsession.implement;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.EssaySolvedFixture;
import com.neogul.whynago.fixture.SolvedMultipleChoiceFixture;
import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.implement.QuestionStatReader;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class QuestionStatAggregatorTest extends IntegrationTestSupport {

    private static final Long QUESTION_ID = 100L;

    @Autowired
    private QuestionStatAggregator questionStatAggregator;

    @Autowired
    private QuestionStatReader questionStatReader;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("객관식 풀이 이력을 문항 단위로 집계해 정답률과 평균 소요 시간을 저장한다.")
    void aggregateAll() {
        // given
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .questionId(QUESTION_ID).isCorrect(true).elapsedSeconds(60).build());
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .questionId(QUESTION_ID).isCorrect(false).elapsedSeconds(100).build());

        // when
        questionStatAggregator.aggregateAll();

        // then
        QuestionStat stat = readStat();
        assertThat(stat.getSampleCount()).isEqualTo(2);
        assertThat(stat.getCorrectRate()).isEqualTo(0.5);
        assertThat(stat.getAvgElapsedSeconds()).isEqualTo(80);
    }

    @Test
    @DisplayName("소요 시간을 보고한 표본만 평균에 넣는다.")
    void aggregateAll_ignoresUnmeasuredElapsedSeconds() {
        // given
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .questionId(QUESTION_ID).elapsedSeconds(60).build());
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .questionId(QUESTION_ID).elapsedSeconds(null).build());

        // when
        questionStatAggregator.aggregateAll();

        // then
        QuestionStat stat = readStat();
        // 미측정을 0초로 세면 평균이 30초로 반토막 난다.
        assertThat(stat.getAvgElapsedSeconds()).isEqualTo(60);
        assertThat(stat.getSampleCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("소요 시간 표본이 하나도 없으면 평균을 남기지 않는다.")
    void aggregateAll_withoutElapsedSeconds() {
        // given
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .questionId(QUESTION_ID).elapsedSeconds(null).build());

        // when
        questionStatAggregator.aggregateAll();

        // then
        assertThat(readStat().getAvgElapsedSeconds()).isNull();
    }

    @Test
    @DisplayName("서술형 꼬리질문은 참조할 문항이 없어 집계에서 빠진다.")
    void aggregateAll_excludesFollowup() {
        // given
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .questionId(QUESTION_ID).isCorrect(true).elapsedSeconds(120).build());
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .type(ItemType.FOLLOWUP).questionId(null).isCorrect(false).elapsedSeconds(600).build());

        // when
        questionStatAggregator.aggregateAll();

        // then
        QuestionStat stat = readStat();
        assertThat(stat.getSampleCount()).isEqualTo(1);
        assertThat(stat.getCorrectRate()).isEqualTo(1.0);
        assertThat(stat.getAvgElapsedSeconds()).isEqualTo(120);
    }

    @Test
    @DisplayName("다시 집계하면 이전 통계를 누적하지 않고 덮어쓴다.")
    void aggregateAll_overwritesPreviousStat() {
        // given
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .questionId(QUESTION_ID).isCorrect(true).elapsedSeconds(60).build());
        questionStatAggregator.aggregateAll();

        // when
        questionStatAggregator.aggregateAll();

        // then
        assertThat(readStat().getSampleCount()).isEqualTo(1);
    }

    private QuestionStat readStat() {
        Map<Long, QuestionStat> stats = questionStatReader.readAll(List.of(QUESTION_ID));
        assertThat(stats).containsKey(QUESTION_ID);
        return stats.get(QUESTION_ID);
    }
}
