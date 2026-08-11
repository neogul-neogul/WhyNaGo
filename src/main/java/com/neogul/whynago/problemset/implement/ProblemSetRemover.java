package com.neogul.whynago.problemset.implement;

import com.neogul.whynago.problemset.domain.ProblemSet;
import com.neogul.whynago.problemset.infra.ProblemSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetRemover {

    private final ProblemSetRepository problemSetRepository;

    public void remove(ProblemSet problemSet) {
        problemSetRepository.delete(problemSet);
    }
}
