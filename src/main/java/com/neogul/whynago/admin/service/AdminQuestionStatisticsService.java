package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.implement.ChoiceDistributionCalculator;
import com.neogul.whynago.admin.implement.dto.ChoiceDistribution;
import com.neogul.whynago.admin.service.dto.MultipleChoiceStatisticsResult;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.implement.MultipleChoiceSolveStatisticsReader;
import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminQuestionStatisticsService {

    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;
    private final MultipleChoiceSolveStatisticsReader multipleChoiceSolveStatisticsReader;
    private final ChoiceDistributionCalculator choiceDistributionCalculator;

    @Transactional(readOnly = true)
    public MultipleChoiceStatisticsResult readMultipleChoiceStatistics(Long questionId) {
        Question question = questionReader.readMultipleChoiceQuestion(questionId);
        List<AnswerChoice> choices = answerChoiceReader.readChoices(question.getId());
        QuestionSolveStatistics statistics = multipleChoiceSolveStatisticsReader.read(question.getId());
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices, statistics);

        return MultipleChoiceStatisticsResult.of(
                question.getId(),
                statistics,
                choiceDistributionCalculator.rate(statistics.correctCount(), statistics.totalSolveCount()),
                distributions,
                choiceDistributionCalculator.findMostChosen(distributions)
        );
    }
}
