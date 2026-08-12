package com.neogul.whynago.problemset.implement;

import com.neogul.whynago.problemset.domain.ProblemSetItem;
import com.neogul.whynago.problemset.infra.ProblemSetItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetItemAppender {

    private final ProblemSetItemRepository problemSetItemRepository;

    /**
     * 이미 담겨 있으면 아무 일도 하지 않는다(멱등). 실제로 새로 담겼는지 여부를 반환해
     * 호출부가 {@code ProblemSet.touch()} 같은 후속 처리를 조건부로 실행할 수 있게 한다.
     */
    public boolean appendIfAbsent(Long problemSetId, Long questionId) {
        if (problemSetItemRepository.existsByProblemSetIdAndQuestionId(problemSetId, questionId)) {
            return false;
        }
        problemSetItemRepository.save(ProblemSetItem.create(problemSetId, questionId));
        return true;
    }
}
