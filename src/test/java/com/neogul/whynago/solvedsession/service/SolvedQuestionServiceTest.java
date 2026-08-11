package com.neogul.whynago.solvedsession.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionIdsResult;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SolvedQuestionServiceTest extends IntegrationTestSupport {

    @Autowired
    private SolvedQuestionService solvedQuestionService;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("객관식과 서술형으로 푼 문제 ID를 한 목록으로 조회한다.")
    void readSolvedQuestionIds() {
        // given
        solvedMultipleChoiceRepository.save(multipleChoice(10L, 100L));
        solvedMultipleChoiceRepository.save(multipleChoice(10L, 101L));
        essaySolvedRepository.save(essay(10L, 200L));

        // when
        SolvedQuestionIdsResult result = solvedQuestionService.readSolvedQuestionIds(10L);

        // then
        assertThat(result.questionIds()).containsExactlyInAnyOrder(100L, 101L, 200L);
    }

    @Test
    @DisplayName("다른 사용자가 푼 문제 ID는 조회되지 않는다.")
    void readSolvedQuestionIds_otherUser() {
        // given
        solvedMultipleChoiceRepository.save(multipleChoice(10L, 100L));
        solvedMultipleChoiceRepository.save(multipleChoice(20L, 900L));
        essaySolvedRepository.save(essay(20L, 901L));

        // when
        SolvedQuestionIdsResult result = solvedQuestionService.readSolvedQuestionIds(10L);

        // then
        assertThat(result.questionIds()).containsExactly(100L);
    }

    @Test
    @DisplayName("푼 문제가 없으면 빈 목록을 조회한다.")
    void readSolvedQuestionIds_noRecord() {
        // when
        SolvedQuestionIdsResult result = solvedQuestionService.readSolvedQuestionIds(10L);

        // then
        assertThat(result.questionIds()).isEmpty();
    }

    private SolvedMultipleChoice multipleChoice(Long userId, Long questionId) {
        return SolvedMultipleChoice.create(
                1L,
                userId,
                questionId,
                ItemType.MAIN,
                1,
                1L,
                1L,
                true,
                LocalDateTime.now()
        );
    }

    private EssaySolved essay(Long userId, Long questionId) {
        return EssaySolved.create(
                1L,
                userId,
                ItemType.MAIN,
                1,
                questionId,
                "질문",
                "답변",
                "피드백",
                "모범답안",
                true,
                LocalDateTime.now()
        );
    }
}
