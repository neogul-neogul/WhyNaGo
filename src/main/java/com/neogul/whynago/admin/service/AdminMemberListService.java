package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.implement.DashboardPeriodCalculator;
import com.neogul.whynago.admin.implement.dto.DateTimeRange;
import com.neogul.whynago.admin.service.dto.AdminMemberResult;
import com.neogul.whynago.admin.service.dto.AdminMemberSearchCommand;
import com.neogul.whynago.admin.service.dto.AdminMemberSummaryResult;
import com.neogul.whynago.admin.service.dto.AdminMembersResult;
import com.neogul.whynago.solvedsession.implement.SolveStatisticsReader;
import com.neogul.whynago.user.implement.UserReader;
import com.neogul.whynago.user.implement.dto.UserPage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMemberListService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserReader userReader;
    private final SolveStatisticsReader solveStatisticsReader;
    private final DashboardPeriodCalculator dashboardPeriodCalculator;

    // 티어·점수는 조회 시점 파생값이라 서버가 정렬·필터할 수 없다. 목록은 가입 역순(id desc) 한 가지 순서만 제공한다.
    @Transactional(readOnly = true)
    public AdminMembersResult readMembers(AdminMemberSearchCommand command) {
        UserPage userPage = userReader.readPage(command.keyword(), command.page(), command.size());
        List<AdminMemberResult> members = userPage.users().stream()
                .map(AdminMemberResult::from)
                .toList();

        return new AdminMembersResult(members, command.page(), command.size(), userPage.totalElements());
    }

    // 대시보드와 같은 Reader·같은 기간 계산기를 쓴다. 두 화면의 "최근 7일 활동 회원 수"가 어긋나지 않아야 한다.
    @Transactional(readOnly = true)
    public AdminMemberSummaryResult readSummary() {
        DateTimeRange recentWeek = dashboardPeriodCalculator.calculate(LocalDate.now(KST)).recentWeekRange();

        return new AdminMemberSummaryResult(
                userReader.countAll(),
                solveStatisticsReader.countActiveUsersBetween(recentWeek.from(), recentWeek.to())
        );
    }
}
