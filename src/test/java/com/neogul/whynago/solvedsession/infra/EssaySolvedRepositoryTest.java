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

    private EssaySolved item(Long solvedSessionId, ItemType type, int sequence, Long questionId) {
        return EssaySolved.create(
                solvedSessionId,
                10L,
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
