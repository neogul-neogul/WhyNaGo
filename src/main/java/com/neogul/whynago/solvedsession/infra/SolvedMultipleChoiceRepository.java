package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolvedMultipleChoiceRepository extends JpaRepository<SolvedMultipleChoice, Long> {

    List<SolvedMultipleChoice> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);

    @Query("select distinct s.questionId from SolvedMultipleChoice s where s.userId = :userId")
    List<Long> findSolvedQuestionIds(@Param("userId") Long userId);

    List<SolvedMultipleChoice> findByUserId(Long userId);

    // 문항 통계 배치용 전역 집계다. 사용자 구분 없이 모든 풀이를 문항 단위로 모은다.
    @Query("""
            select s.questionId as questionId,
                   count(s) as solvedCount,
                   sum(case when s.isCorrect = true then 1L else 0L end) as correctCount,
                   avg(s.elapsedSeconds) as avgElapsedSeconds
            from SolvedMultipleChoice s
            group by s.questionId
            """)
    List<QuestionSolveCount> aggregateByQuestion();
}
