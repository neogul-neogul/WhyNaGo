package com.neogul.whynago.solvedsession.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class SolvedSessionRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Test
    @DisplayName("사용자의 풀이 세션을 완료 시각 내림차순으로 개수만큼 조회한다.")
    void findByUserIdOrderBySolvedAtDesc() {
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 23, 10, 0)));
        SolvedSession latest = solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 25, 10, 0)));
        SolvedSession middle = solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 24, 10, 0)));
        solvedSessionRepository.save(session(20L, LocalDateTime.of(2026, 6, 26, 10, 0)));

        List<SolvedSession> result = solvedSessionRepository.findByUserIdOrderBySolvedAtDesc(10L, PageRequest.of(0, 2));

        assertThat(result).extracting(SolvedSession::getId).containsExactly(latest.getId(), middle.getId());
    }

    @Test
    @DisplayName("size 제한 없이(Pageable.unpaged()) 사용자의 전체 풀이 세션을 조회한다.")
    void findByUserIdOrderBySolvedAtDesc_unpaged() {
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 23, 10, 0)));
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 24, 10, 0)));
        solvedSessionRepository.save(session(20L, LocalDateTime.of(2026, 6, 25, 10, 0)));

        List<SolvedSession> result = solvedSessionRepository.findByUserIdOrderBySolvedAtDesc(10L, Pageable.unpaged());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("완료 시각 범위에 속한 사용자의 풀이 세션만 조회한다.")
    void findByUserIdAndSolvedAtBetween() {
        SolvedSession inRange = solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 24, 10, 0)));
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 6, 20, 10, 0)));
        solvedSessionRepository.save(session(20L, LocalDateTime.of(2026, 6, 24, 10, 0)));

        List<SolvedSession> result = solvedSessionRepository.findByUserIdAndSolvedAtBetween(
                10L,
                LocalDateTime.of(2026, 6, 23, 0, 0),
                LocalDateTime.of(2026, 6, 25, 0, 0)
        );

        assertThat(result).extracting(SolvedSession::getId).containsExactly(inRange.getId());
    }

    private SolvedSession session(Long userId, LocalDateTime solvedAt) {
        return SolvedSession.completed(userId, QuestionType.MULTIPLE_CHOICE, 3, 2, solvedAt.minusMinutes(5), solvedAt);
    }
}