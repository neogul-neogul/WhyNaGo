package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.dto.ChoiceSelectionCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveSummary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolvedMultipleChoiceRepository extends JpaRepository<SolvedMultipleChoice, Long> {

    List<SolvedMultipleChoice> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);

    @Query("select distinct s.questionId from SolvedMultipleChoice s where s.userId = :userId")
    List<Long> findSolvedQuestionIds(@Param("userId") Long userId);

    // 같은 문항이 세션에 따라 본질문이기도 꼬리질문이기도 하므로 ItemType으로 나누지 않는다.
    @Query("""
            select count(s) as totalCount,
                   sum(case when s.isCorrect = true then 1 else 0 end) as correctCount
            from SolvedMultipleChoice s
            where s.questionId = :questionId
            """)
    QuestionSolveSummary findSolveSummary(@Param("questionId") Long questionId);

    @Query("""
            select s.userChoiceId as choiceId, count(s) as selectedCount
            from SolvedMultipleChoice s
            where s.questionId = :questionId
            group by s.userChoiceId
            """)
    List<ChoiceSelectionCount> countGroupByUserChoice(@Param("questionId") Long questionId);
}
