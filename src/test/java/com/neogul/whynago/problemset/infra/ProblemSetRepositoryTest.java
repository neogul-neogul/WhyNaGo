package com.neogul.whynago.problemset.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.problemset.domain.ProblemSet;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProblemSetRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ProblemSetRepository problemSetRepository;

    @Test
    @DisplayName("소유자의 문제집을 ID로 조회한다.")
    void findByIdAndUserId() {
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(1L, "면접 D-7 벼락치기"));

        Optional<ProblemSet> found = problemSetRepository.findByIdAndUserId(problemSet.getId(), 1L);

        assertThat(found).get().extracting(ProblemSet::getName).isEqualTo("면접 D-7 벼락치기");
    }

    @Test
    @DisplayName("다른 사용자 소유의 문제집은 조회되지 않는다.")
    void findByIdAndUserId_notOwner() {
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(1L, "면접 D-7 벼락치기"));

        Optional<ProblemSet> found = problemSetRepository.findByIdAndUserId(problemSet.getId(), 2L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("사용자의 문제집 전체를 최신순으로 조회한다.")
    void findByUserIdOrderByIdDesc() {
        ProblemSet first = problemSetRepository.save(ProblemSet.create(1L, "면접 D-7 벼락치기"));
        ProblemSet second = problemSetRepository.save(ProblemSet.create(1L, "네트워크 집중 보완"));
        problemSetRepository.save(ProblemSet.create(2L, "다른 사용자 문제집"));

        List<ProblemSet> result = problemSetRepository.findByUserIdOrderByIdDesc(1L);

        assertThat(result).extracting(ProblemSet::getId).containsExactly(second.getId(), first.getId());
    }
}
