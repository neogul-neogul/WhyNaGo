package com.neogul.whynago.problemset.implement;

import com.neogul.whynago.problemset.infra.ProblemSetItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetItemRemover {

    private final ProblemSetItemRepository problemSetItemRepository;

    /**
     * 없는 항목을 제거하려 해도 아무 일도 하지 않는다(멱등). 실제로 제거됐는지 여부를 반환한다.
     */
    public boolean remove(Long problemSetId, Long questionId) {
        return problemSetItemRepository.findByProblemSetIdAndQuestionId(problemSetId, questionId)
                .map(item -> {
                    problemSetItemRepository.delete(item);
                    return true;
                })
                .orElse(false);
    }

    public void removeAllByProblemSetId(Long problemSetId) {
        problemSetItemRepository.deleteByProblemSetId(problemSetId);
    }
}
