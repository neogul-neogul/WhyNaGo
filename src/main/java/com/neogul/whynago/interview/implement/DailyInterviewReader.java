package com.neogul.whynago.interview.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.domain.InterviewStatus;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyInterviewReader {

    private final DailyInterviewRepository dailyInterviewRepository;

    public Optional<DailyInterview> readByDate(Long userId, LocalDate date) {
        return dailyInterviewRepository.findByUserIdAndInterviewDate(userId, date);
    }

    public DailyInterview readOwned(Long interviewId, Long userId) {
        DailyInterview interview = dailyInterviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_NOT_FOUND));
        if (!interview.isOwnedBy(userId)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_FOUND);
        }
        return interview;
    }

    public DailyInterview readInProgress(Long interviewId, Long userId) {
        DailyInterview interview = readOwned(interviewId, userId);
        if (!interview.isInProgress()) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_IN_PROGRESS);
        }
        return interview;
    }

    public DailyInterview readCompleted(Long interviewId, Long userId) {
        DailyInterview interview = readOwned(interviewId, userId);
        if (!interview.isCompleted()) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_COMPLETED);
        }
        return interview;
    }

    public List<DailyInterview> readCompletedAll(Long userId) {
        return dailyInterviewRepository.findByUserIdAndStatusOrderByInterviewDateDesc(userId, InterviewStatus.COMPLETED);
    }

    public long countCompleted(Long userId) {
        return dailyInterviewRepository.countByUserIdAndStatus(userId, InterviewStatus.COMPLETED);
    }
}
