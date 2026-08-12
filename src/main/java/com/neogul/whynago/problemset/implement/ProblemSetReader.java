package com.neogul.whynago.problemset.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.problemset.domain.ProblemSet;
import com.neogul.whynago.problemset.exception.ProblemSetErrorCode;
import com.neogul.whynago.problemset.infra.ProblemSetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetReader {

    private final ProblemSetRepository problemSetRepository;

    public ProblemSet read(Long userId, Long problemSetId) {
        return problemSetRepository.findByIdAndUserId(problemSetId, userId)
                .orElseThrow(() -> new BusinessException(ProblemSetErrorCode.PROBLEM_SET_NOT_FOUND));
    }

    public List<ProblemSet> readAll(Long userId) {
        return problemSetRepository.findByUserIdOrderByIdDesc(userId);
    }
}
