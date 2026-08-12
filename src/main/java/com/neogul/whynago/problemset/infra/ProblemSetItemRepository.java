package com.neogul.whynago.problemset.infra;

import com.neogul.whynago.problemset.domain.ProblemSetItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemSetItemRepository extends JpaRepository<ProblemSetItem, Long> {

    boolean existsByProblemSetIdAndQuestionId(Long problemSetId, Long questionId);

    Optional<ProblemSetItem> findByProblemSetIdAndQuestionId(Long problemSetId, Long questionId);

    List<ProblemSetItem> findByProblemSetIdOrderByIdAsc(Long problemSetId);

    List<ProblemSetItem> findByProblemSetIdInOrderByIdAsc(List<Long> problemSetIds);

    void deleteByProblemSetId(Long problemSetId);
}
