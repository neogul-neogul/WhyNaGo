package com.neogul.whynago.solvedsession.infra;

import static org.assertj.core.api.Assertions.assertThat;
import com.neogul.whynago.fixture.SolvedMultipleChoiceFixture;

import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SolvedMultipleChoiceRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("사용자가 푼 객관식 문제 ID를 중복 없이 조회한다.")
    void findSolvedQuestionIds() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L));
        solvedMultipleChoiceRepository.save(item(10L, 101L));
        solvedMultipleChoiceRepository.save(item(10L, 100L));

        // when
        List<Long> questionIds = solvedMultipleChoiceRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("다른 사용자가 푼 문제 ID는 조회되지 않는다.")
    void findSolvedQuestionIds_otherUser() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L));
        solvedMultipleChoiceRepository.save(item(20L, 200L));

        // when
        List<Long> questionIds = solvedMultipleChoiceRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactly(100L);
    }

    @Test
    @DisplayName("푼 문제가 없으면 빈 목록을 조회한다.")
    void findSolvedQuestionIds_noRecord() {
        // when
        List<Long> questionIds = solvedMultipleChoiceRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).isEmpty();
    }

    private SolvedMultipleChoice item(Long userId, Long questionId) {
        return SolvedMultipleChoiceFixture.builder()
                .userId(userId)
                .questionId(questionId)
                .build();
    }
}