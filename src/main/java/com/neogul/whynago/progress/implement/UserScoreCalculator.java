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

/**
 * 본질문+꼬리질문을 모두 맞힌(correctCount == totalCount) 세션만 점수 대상이다.
 * 객관식은 세션에 등장한 문항(본질문·꼬리질문)이 전부 실제 독립된 Question이라 문항마다 자기 난이도로 점수를 받고,
 * 서술형은 꼬리질문이 AI가 그때그때 생성하는 스냅샷이라 실제 Question이 없어 본질문 하나로만 점수를 받는다
 * (→ docs/DOMAIN.md 꼬리질문 분기·서술형 꼬리질문 생성 정책). 같은 Question은 유저 인생 전체에서 최초로
 * 만점을 받은 시점에만 점수를 지급한다.
 */
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
