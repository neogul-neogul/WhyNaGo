package com.neogul.whynago.solvedsession.service;

import com.neogul.whynago.solvedsession.implement.QuestionStatAggregator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionStatService {

    private final QuestionStatAggregator questionStatAggregator;

    @Transactional
    public int refreshAll() {
        return questionStatAggregator.aggregateAll();
    }
}
