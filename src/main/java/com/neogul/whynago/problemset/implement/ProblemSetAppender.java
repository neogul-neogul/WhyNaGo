package com.neogul.whynago.problemset.implement;

import com.neogul.whynago.problemset.domain.ProblemSet;
import com.neogul.whynago.problemset.infra.ProblemSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetAppender {

    private final ProblemSetRepository problemSetRepository;

    public ProblemSet append(Long userId, String name) {
        return problemSetRepository.save(ProblemSet.create(userId, name));
    }
}
