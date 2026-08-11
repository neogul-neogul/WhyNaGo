package com.neogul.whynago.progress.service;

import com.neogul.whynago.interview.service.InterviewService;
import com.neogul.whynago.learningrecord.service.LearningRecordService;
import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.implement.UserScoreCalculator;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import com.neogul.whynago.progress.service.dto.ProgressSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserScoreCalculator userScoreCalculator;
    private final LearningRecordService learningRecordService;
    private final InterviewService interviewService;

    @Transactional(readOnly = true)
    public ProgressDetailResult getDetail(Long userId) {
        UserProgressAggregate aggregate = userScoreCalculator.calculate(userId);
        Tier tier = Tier.from(aggregate.totalScore());
        return ProgressDetailResult.of(aggregate, tier);
    }

    @Transactional(readOnly = true)
    public ProgressSummaryResult getSummary(Long userId) {
        UserProgressAggregate aggregate = userScoreCalculator.calculate(userId);
        int completedInterviewCount = interviewService.countCompleted(userId);
        return ProgressSummaryResult.of(learningRecordService.getStreak(userId), aggregate, completedInterviewCount);
    }
}
