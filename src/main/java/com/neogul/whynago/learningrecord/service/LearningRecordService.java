package com.neogul.whynago.learningrecord.service;

import com.neogul.whynago.learningrecord.implement.DailyRecordAggregator;
import com.neogul.whynago.learningrecord.implement.StreakCalculator;
import com.neogul.whynago.learningrecord.service.dto.DailyRecordCountResult;
import com.neogul.whynago.learningrecord.service.dto.RecentRecordResult;
import com.neogul.whynago.learningrecord.service.dto.StreakResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySolvedReader;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceReader;
import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningRecordService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_DAILY_COUNT_RANGE_DAYS = 364;

    private final SolvedSessionReader solvedSessionReader;
    private final SolvedMultipleChoiceReader solvedMultipleChoiceReader;
    private final EssaySolvedReader essaySolvedReader;
    private final QuestionReader questionReader;
    private final StreakCalculator streakCalculator;
    private final DailyRecordAggregator dailyRecordAggregator;

    @Transactional(readOnly = true)
    public List<RecentRecordResult> findRecent(Long userId, int size) {
        return solvedSessionReader.readRecent(userId, size).stream()
                .map(this::toRecentRecord)
                .toList();
    }

    @Transactional(readOnly = true)
    public StreakResult getStreak(Long userId) {
        List<LocalDate> solvedDates = solvedSessionReader.readAll(userId).stream()
                .map(session -> session.getSolvedAt().toLocalDate())
                .toList();
        LocalDate today = LocalDate.now(KST);
        return StreakResult.from(streakCalculator.calculate(solvedDates, today));
    }

    @Transactional(readOnly = true)
    public List<DailyRecordCountResult> findDailyCounts(Long userId, LocalDate from, LocalDate to) {
        LocalDate resolvedTo = to != null ? to : LocalDate.now(KST);
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(DEFAULT_DAILY_COUNT_RANGE_DAYS);
        if (resolvedFrom.isAfter(resolvedTo)) {
            return List.of();
        }

        LocalDateTime fromDateTime = resolvedFrom.atStartOfDay();
        LocalDateTime toDateTime = resolvedTo.atTime(LocalTime.MAX);
        List<SolvedSession> sessions = solvedSessionReader.readBetween(userId, fromDateTime, toDateTime);

        return dailyRecordAggregator.aggregate(sessions).stream()
                .map(DailyRecordCountResult::from)
                .toList();
    }

    private RecentRecordResult toRecentRecord(SolvedSession session) {
        Question rootQuestion = readRootQuestion(session);
        return RecentRecordResult.of(session, rootQuestion.getCategory());
    }

    private Question readRootQuestion(SolvedSession session) {
        if (session.getType() == QuestionType.ESSAY) {
            Long rootQuestionId = essaySolvedReader.readOrdered(session.getId()).get(0).getQuestionId();
            return questionReader.read(rootQuestionId);
        }
        Long rootQuestionId = solvedMultipleChoiceReader.readOrdered(session.getId()).get(0).getQuestionId();
        return questionReader.read(rootQuestionId);
    }
}