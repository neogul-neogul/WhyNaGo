package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.service.dto.AdminMemberDetailResult;
import com.neogul.whynago.admin.service.dto.AdminMemberResult;
import com.neogul.whynago.interview.implement.DailyInterviewReader;
import com.neogul.whynago.learningrecord.implement.SolvedDateReader;
import com.neogul.whynago.learningrecord.implement.StreakCalculator;
import com.neogul.whynago.learningrecord.implement.dto.StreakSummary;
import com.neogul.whynago.solvedsession.implement.SolveStatisticsReader;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.implement.UserReader;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 스트릭·풀이 수·면접 수는 목록 컬럼에 없고 상세 모달을 열 때만 필요해 단건 조회로 분리했다.
@Service
@RequiredArgsConstructor
public class AdminMemberDetailService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserReader userReader;
    private final SolvedDateReader solvedDateReader;
    private final StreakCalculator streakCalculator;
    private final SolveStatisticsReader solveStatisticsReader;
    private final DailyInterviewReader dailyInterviewReader;

    @Transactional(readOnly = true)
    public AdminMemberDetailResult readMember(Long userId) {
        User user = userReader.read(userId);
        StreakSummary streak = streakCalculator.calculate(solvedDateReader.readAll(userId), LocalDate.now(KST));

        return new AdminMemberDetailResult(
                AdminMemberResult.from(user),
                streak.streakDays(),
                solveStatisticsReader.countQuestionsByUser(userId),
                dailyInterviewReader.countCompleted(userId)
        );
    }
}
