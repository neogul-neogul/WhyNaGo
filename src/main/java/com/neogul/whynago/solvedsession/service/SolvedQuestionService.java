package com.neogul.whynago.solvedsession.service;

import com.neogul.whynago.solvedsession.implement.SolvedQuestionIdReader;
import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionIdsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolvedQuestionService {

    private final SolvedQuestionIdReader solvedQuestionIdReader;

    public SolvedQuestionIdsResult readSolvedQuestionIds(Long userId) {
        return SolvedQuestionIdsResult.from(solvedQuestionIdReader.readAll(userId));
    }
}