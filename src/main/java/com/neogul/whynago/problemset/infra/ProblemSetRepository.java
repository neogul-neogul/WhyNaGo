package com.neogul.whynago.problemset.infra;

import com.neogul.whynago.problemset.domain.ProblemSet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemSetRepository extends JpaRepository<ProblemSet, Long> {

    Optional<ProblemSet> findByIdAndUserId(Long id, Long userId);

    List<ProblemSet> findByUserIdOrderByIdDesc(Long userId);
}
