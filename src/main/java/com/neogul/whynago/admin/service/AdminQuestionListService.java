package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.implement.ChoiceDistributionCalculator;
import com.neogul.whynago.admin.service.dto.AdminQuestionResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionsResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.implement.dto.QuestionPage;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import com.neogul.whynago.solvedsession.implement.EssaySolveStatisticsReader;
import com.neogul.whynago.solvedsession.implement.MultipleChoiceSolveStatisticsReader;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminQuestionListService {

    private final QuestionReader questionReader;
    private final MultipleChoiceSolveStatisticsReader multipleChoiceSolveStatisticsReader;
    private final EssaySolveStatisticsReader essaySolveStatisticsReader;
    private final ChoiceDistributionCalculator choiceDistributionCalculator;

    @Transactional(readOnly = true)
    public AdminQuestionsResult readQuestions(QuestionSearchCommand command) {
        QuestionPage questionPage = questionReader.readQuestionPageForAdmin(
                command.type(),
                command.difficulty(),
                command.category(),
                command.keyword(),
                command.page(),
                command.size()
        );
        List<Question> questions = questionPage.questions();
        Map<Long, QuestionSolveCount> solveCountByQuestionId = readSolveCounts(questions);

        List<AdminQuestionResult> results = questions.stream()
                .map(question -> toResult(question, solveCountByQuestionId.get(question.getId())))
                .toList();

        return new AdminQuestionsResult(results, command.page(), command.size(), questionPage.totalElements());
    }

    // 객관식·서술형은 풀이 이력 테이블이 달라 유형별로 나눠 벌크 집계한다.
    private Map<Long, QuestionSolveCount> readSolveCounts(List<Question> questions) {
        List<Long> multipleChoiceIds = questions.stream()
                .filter(Question::isMultipleChoice)
                .map(Question::getId)
                .toList();
        List<Long> essayIds = questions.stream()
                .filter(Question::isEssay)
                .map(Question::getId)
                .toList();

        Map<Long, QuestionSolveCount> solveCountByQuestionId = new HashMap<>();
        solveCountByQuestionId.putAll(multipleChoiceSolveStatisticsReader.readBulk(multipleChoiceIds));
        solveCountByQuestionId.putAll(essaySolveStatisticsReader.readBulk(essayIds));
        return solveCountByQuestionId;
    }

    private AdminQuestionResult toResult(Question question, QuestionSolveCount count) {
        if (count == null) {
            return AdminQuestionResult.of(question, 0L, null);
        }
        long correctCount = count.getCorrectCount() == null ? 0L : count.getCorrectCount();
        double correctRate = choiceDistributionCalculator.rate(correctCount, count.getTotalCount());
        return AdminQuestionResult.of(question, count.getTotalCount(), correctRate);
    }
}
