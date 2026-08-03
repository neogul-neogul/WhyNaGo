package com.neogul.whynago.learningrecord.service;

import com.neogul.whynago.learningrecord.implement.DailyRecordAggregator;
import com.neogul.whynago.learningrecord.implement.DailyRecordRangeCalculator;
import com.neogul.whynago.learningrecord.implement.RootQuestionReader;
import com.neogul.whynago.learningrecord.implement.SolvedDateReader;
import com.neogul.whynago.learningrecord.implement.StreakCalculator;
import com.neogul.whynago.learningrecord.implement.dto.DateTimeRange;
import com.neogul.whynago.learningrecord.service.dto.DailyRecordCountResult;
import com.neogul.whynago.learningrecord.service.dto.RecentRecordResult;
import com.neogul.whynago.learningrecord.service.dto.StreakResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningRecordService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SolvedSessionReader solvedSessionReader;
    private final RootQuestionReader rootQuestionReader;
    private final SolvedDateReader solvedDateReader;
    private final StreakCalculator streakCalculator;
    private final DailyRecordAggregator dailyRecordAggregator;
    private final DailyRecordRangeCalculator dailyRecordRangeCalculator;

    @Transactional(readOnly = true)
    public List<RecentRecordResult> findRecent(Long userId, int size) {
        return solvedSessionReader.readRecent(userId, size).stream()
                .map(this::toRecentRecord)
                .toList();
    }

    @Transactional(readOnly = true)
    public StreakResult getStreak(Long userId) {
        List<LocalDate> solvedDates = solvedDateReader.readAll(userId);
        return StreakResult.from(streakCalculator.calculate(solvedDates, LocalDate.now(KST)));
    }

    @Transactional(readOnly = true)
    public List<DailyRecordCountResult> findDailyCounts(Long userId, LocalDate from, LocalDate to) {
        Optional<DateTimeRange> range = dailyRecordRangeCalculator.resolve(from, to, LocalDate.now(KST));
        if (range.isEmpty()) {
            return List.of();
        }

        List<SolvedSession> sessions = solvedSessionReader.readBetween(userId, range.get().from(), range.get().to());
        return dailyRecordAggregator.aggregate(sessions).stream()
                .map(DailyRecordCountResult::from)
                .toList();
    }

    private RecentRecordResult toRecentRecord(SolvedSession session) {
        Question rootQuestion = rootQuestionReader.read(session);
        return RecentRecordResult.of(session, rootQuestion.getCategory());
    }
}
