package com.neogul.whynago.solvedsession.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EssaySolvedRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("풀이 세션의 서술형 문항을 순서대로 조회한다.")
    void findBySolvedSessionIdOrderBySequence() {
        LocalDateTime solvedAt = LocalDateTime.now();
        essaySolvedRepository.save(item(1L, ItemType.FOLLOWUP, 3, null));
        essaySolvedRepository.save(item(1L, ItemType.MAIN, 1, 100L));
        essaySolvedRepository.save(item(1L, ItemType.FOLLOWUP, 2, null));
        essaySolvedRepository.save(item(2L, ItemType.MAIN, 1, 200L));

        List<EssaySolved> items = essaySolvedRepository.findBySolvedSessionIdOrderBySequence(1L);

        assertThat(items).extracting(EssaySolved::getSequence).containsExactly(1, 2, 3);
        assertThat(items.get(0).getType()).isEqualTo(ItemType.MAIN);
        assertThat(items.get(0).getQuestionId()).isEqualTo(100L);
        assertThat(items.get(1).getQuestionId()).isNull();
    }

    @Test
    @DisplayName("긴 텍스트 스냅샷 필드를 저장하고 조회한다.")
    void saveLongText() {
        String longAnswer = "가".repeat(2000);
        EssaySolved saved = essaySolvedRepository.save(EssaySolved.create(
                1L,
                10L,
                ItemType.MAIN,
                1,
                100L,
                "트랜잭션 격리 수준을 설명하라.",
                longAnswer,
                "피드백",
                "모범답안",
                true,
                LocalDateTime.now()
        ));

        EssaySolved found = essaySolvedRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUserAnswer()).isEqualTo(longAnswer);
    }

    @Test
    @DisplayName("사용자가 푼 서술형 본질문 ID를 중복 없이 조회한다.")
    void findSolvedQuestionIds() {
        // given
        essaySolvedRepository.save(item(1L, 10L, ItemType.MAIN, 1, 100L));
        essaySolvedRepository.save(item(2L, 10L, ItemType.MAIN, 1, 101L));
        essaySolvedRepository.save(item(3L, 10L, ItemType.MAIN, 1, 100L));

        // when
        List<Long> questionIds = essaySolvedRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("questionId가 없는 서술형 꼬리질문은 푼 문제 ID로 조회되지 않는다.")
    void findSolvedQuestionIds_followup() {
        // given
        essaySolvedRepository.save(item(1L, 10L, ItemType.MAIN, 1, 100L));
        essaySolvedRepository.save(item(1L, 10L, ItemType.FOLLOWUP, 2, null));

        // when
        List<Long> questionIds = essaySolvedRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactly(100L);
    }

    @Test
    @DisplayName("다른 사용자가 푼 서술형 문제 ID는 조회되지 않는다.")
    void findSolvedQuestionIds_otherUser() {
        // given
        essaySolvedRepository.save(item(1L, 10L, ItemType.MAIN, 1, 100L));
        essaySolvedRepository.save(item(2L, 20L, ItemType.MAIN, 1, 200L));

        // when
        List<Long> questionIds = essaySolvedRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactly(100L);
    }

    private EssaySolved item(Long solvedSessionId, ItemType type, int sequence, Long questionId) {
        return item(solvedSessionId, 10L, type, sequence, questionId);
    }

    private EssaySolved item(Long solvedSessionId, Long userId, ItemType type, int sequence, Long questionId) {
        return EssaySolved.create(
                solvedSessionId,
                userId,
                type,
                sequence,
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
