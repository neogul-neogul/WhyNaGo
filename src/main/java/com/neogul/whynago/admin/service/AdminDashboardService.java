package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.implement.DashboardAlertDetector;
import com.neogul.whynago.admin.implement.DashboardPeriodCalculator;
import com.neogul.whynago.admin.implement.dto.DashboardPeriods;
import com.neogul.whynago.admin.implement.dto.DateTimeRange;
import com.neogul.whynago.admin.service.dto.CumulativeSolveCount;
import com.neogul.whynago.admin.service.dto.DashboardResult;
import com.neogul.whynago.admin.service.dto.InterviewMetric;
import com.neogul.whynago.admin.service.dto.MetricComparison;
import com.neogul.whynago.interview.implement.InterviewStatisticsReader;
import com.neogul.whynago.interview.implement.dto.DailyInterviewCount;
import com.neogul.whynago.solvedsession.implement.SolveStatisticsReader;
import com.neogul.whynago.user.implement.UserReader;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserReader userReader;
    private final SolveStatisticsReader solveStatisticsReader;
    private final InterviewStatisticsReader interviewStatisticsReader;
    private final DashboardPeriodCalculator dashboardPeriodCalculator;
    private final DashboardAlertDetector dashboardAlertDetector;

    @Transactional(readOnly = true)
    public DashboardResult readDashboard() {
        DashboardPeriods periods = dashboardPeriodCalculator.calculate(LocalDate.now(KST));

        return new DashboardResult(
                userReader.countAll(),
                readActiveMemberCount(periods),
                CumulativeSolveCount.from(solveStatisticsReader.countCumulativeByType()),
                readSolveCount(periods),
                readSignUpCount(periods),
                readInterviewMetric(periods),
                dashboardAlertDetector.detect(periods.today())
        );
    }

    private MetricComparison readActiveMemberCount(DashboardPeriods periods) {
        return new MetricComparison(
                countActiveMembers(periods.recentWeekRange()),
                countActiveMembers(periods.previousWeekRange())
        );
    }

    private MetricComparison readSolveCount(DashboardPeriods periods) {
        return new MetricComparison(
                countSolvedQuestions(periods.todayRange()),
                countSolvedQuestions(periods.yesterdayRange())
        );
    }

    private MetricComparison readSignUpCount(DashboardPeriods periods) {
        return new MetricComparison(
                countSignUps(periods.todayRange()),
                countSignUps(periods.yesterdayRange())
        );
    }

    private InterviewMetric readInterviewMetric(DashboardPeriods periods) {
        DailyInterviewCount today = interviewStatisticsReader.countByDate(periods.today());
        DailyInterviewCount yesterday = interviewStatisticsReader.countByDate(periods.yesterday());

        return new InterviewMetric(
                new MetricComparison(today.startedCount(), yesterday.startedCount()),
                new MetricComparison(today.completedCount(), yesterday.completedCount())
        );
    }

    private long countActiveMembers(DateTimeRange range) {
        return solveStatisticsReader.countActiveUsersBetween(range.from(), range.to());
    }

    private long countSolvedQuestions(DateTimeRange range) {
        return solveStatisticsReader.countQuestionsBetween(range.from(), range.to());
    }

    private long countSignUps(DateTimeRange range) {
        return userReader.countSignedUpBetween(range.from(), range.to());
    }
}
