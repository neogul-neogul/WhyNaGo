package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.implement.ChoiceDistributionCalculator;
import com.neogul.whynago.admin.service.dto.AdminChoiceResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionDetailResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.implement.EssaySolveStatisticsReader;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminQuestionDetailService {

    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;
    private final EssaySolveStatisticsReader essaySolveStatisticsReader;
    private final ChoiceDistributionCalculator choiceDistributionCalculator;

    @Transactional(readOnly = true)
    public AdminQuestionDetailResult readQuestion(Long questionId) {
        Question question = questionReader.read(questionId);
        List<AdminChoiceResult> choices = readChoices(question);
        List<String> tags = questionReader.readTagNames(List.of(question.getId()))
                .getOrDefault(question.getId(), List.of());

        if (!question.isEssay()) {
            return AdminQuestionDetailResult.of(question, choices, tags, null, null);
        }
        QuestionSolveCount count = essaySolveStatisticsReader.read(question.getId());
        return AdminQuestionDetailResult.of(question, choices, tags, solveCount(count), correctRate(count));
    }

    // 객관식은 선택지에 정답 여부를 그대로 노출한다 — 사용자용 응답과 달리 관리자는 정답을 볼 수 있다.
    // 서술형은 선택지가 없으므로 조회하지 않는다.
    private List<AdminChoiceResult> readChoices(Question question) {
        if (question.isEssay()) {
            return List.of();
        }
        return answerChoiceReader.readChoices(question.getId()).stream()
                .map(AdminChoiceResult::from)
                .toList();
    }

    private long solveCount(QuestionSolveCount count) {
        return count == null ? 0L : count.getTotalCount();
    }

    private Double correctRate(QuestionSolveCount count) {
        if (count == null) {
            return null;
        }
        long correctCount = count.getCorrectCount() == null ? 0L : count.getCorrectCount();
        return choiceDistributionCalculator.rate(correctCount, count.getTotalCount());
    }
}
