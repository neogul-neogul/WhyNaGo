package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EssaySolvedRepository extends JpaRepository<EssaySolved, Long> {

    List<EssaySolved> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);

    // 꼬리질문은 AI가 생성해 참조할 Question이 없으므로(questionId is null) 제외한다.
    @Query("select distinct e.questionId from EssaySolved e where e.userId = :userId and e.questionId is not null")
    List<Long> findSolvedQuestionIds(@Param("userId") Long userId);
}
