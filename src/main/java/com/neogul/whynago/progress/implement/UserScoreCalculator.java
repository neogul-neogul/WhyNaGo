package com.neogul.whynago.progress.implement;

import com.neogul.whynago.progress.domain.ScorePolicy;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySolvedReader;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceReader;
import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserScoreCalculator {

    private final SolvedSessionReader solvedSessionReader;
    private final SolvedMultipleChoiceReader solvedMultipleChoiceReader;
    private final EssaySolvedReader essaySolvedReader;
    private final QuestionReader questionReader;

    public UserProgressAggregate calculate(Long userId) {
        List<SolvedSession> sessions = solvedSessionReader.readAll(userId);

        int totalQuestionCount = 0;
        int totalCorrectCount = 0;
        int totalScore = 0;
        Set<Long> scoredQuestionIds = new HashSet<>();
        Map<Category, Set<Long>> attemptedQuestionIdsByCategory = new HashMap<>();

        for (SolvedSession session : sessions) {
            totalQuestionCount += session.getTotalCount();
            totalCorrectCount += session.getCorrectCount();
            boolean fullyCorrect = session.getCorrectCount() == session.getTotalCount();

            for (Question question : readScorableQuestions(session)) {
                attemptedQuestionIdsByCategory
                        .computeIfAbsent(question.getCategory(), category -> new HashSet<>())
                        .add(question.getId());

                if (fullyCorrect && scoredQuestionIds.add(question.getId())) {
                    totalScore += ScorePolicy.score(question.getType(), question.getDifficulty());
                }
            }
        }

        Map<Category, Integer> categoryQuestionCounts = attemptedQuestionIdsByCategory.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        return new UserProgressAggregate(totalScore, totalQuestionCount, totalCorrectCount, categoryQuestionCounts);
    }

    // 객관식은 본질문+꼬리질문 전부가 채점·집계 대상 Question이고, 서술형은 본질문만 실제 Question을 갖는다.
    private List<Question> readScorableQuestions(SolvedSession session) {
        if (session.getType() == QuestionType.ESSAY) {
            return essaySolvedReader.readOrdered(session.getId()).stream()
                    .filter(item -> item.getType() == ItemType.MAIN)
                    .map(item -> questionReader.read(item.getQuestionId()))
                    .toList();
        }
        return solvedMultipleChoiceReader.readOrdered(session.getId()).stream()
                .map(item -> questionReader.read(item.getQuestionId()))
                .toList();
    }
}
