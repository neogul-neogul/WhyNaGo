package com.neogul.whynago.solvedsession.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.dto.TypeSolveCount;
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

    @Test
    @DisplayName("기간에 속한 전체 사용자의 풀이 문항 수를 합산한다.")
    void sumQuestionCountBetween() {
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 8, 19, 9, 0)));
        solvedSessionRepository.save(session(20L, LocalDateTime.of(2026, 8, 19, 23, 59, 59)));
        solvedSessionRepository.save(session(30L, LocalDateTime.of(2026, 8, 18, 23, 59, 59)));

        Long sum = solvedSessionRepository.sumQuestionCountBetween(
                LocalDateTime.of(2026, 8, 19, 0, 0),
                LocalDateTime.of(2026, 8, 19, 23, 59, 59, 999_999_999)
        );

        assertThat(sum).isEqualTo(6);
    }

    @Test
    @DisplayName("기간에 속한 풀이 세션이 없으면 문항 수 합은 null이다.")
    void sumQuestionCountBetween_noSession() {
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 8, 18, 9, 0)));

        Long sum = solvedSessionRepository.sumQuestionCountBetween(
                LocalDateTime.of(2026, 8, 19, 0, 0),
                LocalDateTime.of(2026, 8, 19, 23, 59, 59, 999_999_999)
        );

        assertThat(sum).isNull();
    }

    @Test
    @DisplayName("누적 풀이 문항 수를 문제 유형별로 합산한다.")
    void sumQuestionCountGroupByType() {
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 8, 19, 9, 0)));
        solvedSessionRepository.save(session(20L, LocalDateTime.of(2026, 8, 18, 9, 0)));
        solvedSessionRepository.save(essaySession(30L, LocalDateTime.of(2026, 8, 19, 9, 0)));

        List<TypeSolveCount> counts = solvedSessionRepository.sumQuestionCountGroupByType();

        assertThat(counts)
                .extracting(TypeSolveCount::getType, TypeSolveCount::getQuestionCount)
                .containsExactlyInAnyOrder(
                        tuple(QuestionType.MULTIPLE_CHOICE, 6L),
                        tuple(QuestionType.ESSAY, 1L)
                );
    }

    @Test
    @DisplayName("기간에 풀이한 회원 수는 같은 회원을 중복해서 세지 않는다.")
    void countActiveUsersBetween() {
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 8, 19, 9, 0)));
        solvedSessionRepository.save(session(10L, LocalDateTime.of(2026, 8, 19, 20, 0)));
        solvedSessionRepository.save(session(20L, LocalDateTime.of(2026, 8, 19, 21, 0)));
        solvedSessionRepository.save(session(30L, LocalDateTime.of(2026, 8, 18, 9, 0)));

        long count = solvedSessionRepository.countActiveUsersBetween(
                LocalDateTime.of(2026, 8, 19, 0, 0),
                LocalDateTime.of(2026, 8, 19, 23, 59, 59, 999_999_999)
        );

        assertThat(count).isEqualTo(2);
    }

    private SolvedSession session(Long userId, LocalDateTime solvedAt) {
        return SolvedSession.completed(userId, QuestionType.MULTIPLE_CHOICE, 3, 2, solvedAt.minusMinutes(5), solvedAt);
    }

    private SolvedSession essaySession(Long userId, LocalDateTime solvedAt) {
        return SolvedSession.completed(userId, QuestionType.ESSAY, 1, 1, solvedAt.minusMinutes(5), solvedAt);
    }
}