package com.neogul.whynago.problemset.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.problemset.domain.ProblemSetItem;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class ProblemSetItemRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ProblemSetItemRepository problemSetItemRepository;

    @Test
    @DisplayName("문제집에 문제가 담겨 있는지 확인한다.")
    void existsByProblemSetIdAndQuestionId() {
        problemSetItemRepository.save(ProblemSetItem.create(1L, 100L));

        assertThat(problemSetItemRepository.existsByProblemSetIdAndQuestionId(1L, 100L)).isTrue();
        assertThat(problemSetItemRepository.existsByProblemSetIdAndQuestionId(1L, 200L)).isFalse();
    }

    @Test
    @DisplayName("같은 문제집에 같은 문제를 중복으로 담으면 예외가 발생한다.")
    void create_duplicateQuestionInSameProblemSet() {
        problemSetItemRepository.save(ProblemSetItem.create(1L, 100L));

        assertThatThrownBy(() -> problemSetItemRepository.saveAndFlush(ProblemSetItem.create(1L, 100L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("문제집에 담긴 문제를 추가한 순서대로 조회한다.")
    void findByProblemSetIdOrderByIdAsc() {
        ProblemSetItem first = problemSetItemRepository.save(ProblemSetItem.create(1L, 100L));
        ProblemSetItem second = problemSetItemRepository.save(ProblemSetItem.create(1L, 101L));
        problemSetItemRepository.save(ProblemSetItem.create(2L, 102L));

        List<ProblemSetItem> result = problemSetItemRepository.findByProblemSetIdOrderByIdAsc(1L);

        assertThat(result).extracting(ProblemSetItem::getId).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("여러 문제집의 담긴 문제를 한 번에 조회한다.")
    void findByProblemSetIdInOrderByIdAsc() {
        problemSetItemRepository.save(ProblemSetItem.create(1L, 100L));
        problemSetItemRepository.save(ProblemSetItem.create(2L, 101L));
        problemSetItemRepository.save(ProblemSetItem.create(3L, 102L));

        List<ProblemSetItem> result = problemSetItemRepository.findByProblemSetIdInOrderByIdAsc(List.of(1L, 2L));

        assertThat(result).extracting(ProblemSetItem::getQuestionId).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("문제집의 담긴 문제를 전부 삭제한다.")
    void deleteByProblemSetId() {
        problemSetItemRepository.save(ProblemSetItem.create(1L, 100L));
        problemSetItemRepository.save(ProblemSetItem.create(1L, 101L));
        problemSetItemRepository.save(ProblemSetItem.create(2L, 102L));

        problemSetItemRepository.deleteByProblemSetId(1L);

        assertThat(problemSetItemRepository.findByProblemSetIdOrderByIdAsc(1L)).isEmpty();
        assertThat(problemSetItemRepository.findByProblemSetIdOrderByIdAsc(2L)).hasSize(1);
    }
}
