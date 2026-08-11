package com.neogul.whynago.interview.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import com.neogul.whynago.question.implement.EssayConversationReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewCanceller {

    private final DailyInterviewRepository dailyInterviewRepository;
    private final EssayConversationReader essayConversationReader;

    public void cancel(DailyInterview interview) {
        int gradedTurns = essayConversationReader.completedTurns(interview.getConversationId());
        if (!interview.isCancelable(gradedTurns)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_CANCELABLE);
        }
        dailyInterviewRepository.delete(interview);
    }
}
