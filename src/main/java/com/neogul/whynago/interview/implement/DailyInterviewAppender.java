package com.neogul.whynago.interview.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import com.neogul.whynago.question.domain.Question;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyInterviewAppender {

    private final DailyInterviewRepository dailyInterviewRepository;

    public DailyInterview append(Long userId, LocalDate today, Question question, String conversationId, LocalDateTime startedAt) {
        if (dailyInterviewRepository.existsByUserIdAndInterviewDate(userId, today)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_ALREADY_STARTED_TODAY);
        }
        DailyInterview interview = DailyInterview.start(
                userId,
                today,
                question.getCategory(),
                question.getId(),
                conversationId,
                startedAt
        );
        try {
            return dailyInterviewRepository.saveAndFlush(interview);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_ALREADY_STARTED_TODAY, e);
        }
    }
}
