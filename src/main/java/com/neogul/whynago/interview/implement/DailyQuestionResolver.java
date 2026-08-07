package com.neogul.whynago.interview.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.infra.DailyInterviewQuestionRepository;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.implement.QuestionReader;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyQuestionResolver {

    private final DailyInterviewQuestionRepository dailyInterviewQuestionRepository;
    private final QuestionReader questionReader;

    public Question resolve(LocalDate today) {
        return questionReader.read(resolveQuestionId(today));
    }

    private Long resolveQuestionId(LocalDate today) {
        return dailyInterviewQuestionRepository.findById(today)
                .map(DailyInterviewQuestion::getQuestionId)
                .orElseGet(() -> pinToday(today));
    }

    private Long pinToday(LocalDate today) {
        List<Question> candidates = questionReader.readQuestions(QuestionType.ESSAY, null, null, null);
        if (candidates.isEmpty()) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_AVAILABLE);
        }
        Long questionId = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).getId();
        dailyInterviewQuestionRepository.save(DailyInterviewQuestion.pin(today, questionId));
        return questionId;
    }
}
