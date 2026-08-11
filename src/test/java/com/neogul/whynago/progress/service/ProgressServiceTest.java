package com.neogul.whynago.progress.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProgressServiceTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;
    private static final LocalDateTime SOLVED_AT = LocalDateTime.of(2026, 6, 25, 10, 0);

    @Autowired
    private ProgressService progressService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("풀이 기록이 없으면 브론즈 티어이고 다음 티어까지 58점이 필요하다.")
    void getDetail_noRecords() {
        ProgressDetailResult result = progressService.getDetail(USER_ID);

        assertThat(result.score()).isZero();
        assertThat(result.tier()).isEqualTo(Tier.BRONZE);
        assertThat(result.nextTier()).isEqualTo(Tier.SILVER);
        assertThat(result.scoreToNextTier()).isEqualTo(58);
        assertThat(result.totalQuestionCount()).isZero();
        assertThat(result.categoryQuestionCounts()).isEmpty();
    }

    @Test
    @DisplayName("만점 세션으로 얻은 점수에 맞는 티어와 다음 티어까지 필요한 점수를 계산한다.")
    void getDetail_withScore() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(USER_ID, QuestionType.MULTIPLE_CHOICE, 1, 1, SOLVED_AT.minusMinutes(5), SOLVED_AT));
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                session.getId(), USER_ID, root.getId(), ItemType.MAIN, 1, choice.getId(), choice.getId(), true, SOLVED_AT));

        ProgressDetailResult result = progressService.getDetail(USER_ID);

        assertThat(result.score()).isEqualTo(2);
        assertThat(result.tier()).isEqualTo(Tier.BRONZE);
        assertThat(result.nextTier()).isEqualTo(Tier.SILVER);
        assertThat(result.scoreToNextTier()).isEqualTo(56);
        assertThat(result.totalQuestionCount()).isEqualTo(1);
    }
}
