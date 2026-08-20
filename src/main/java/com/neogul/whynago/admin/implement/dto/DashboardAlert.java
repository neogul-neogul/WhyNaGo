package com.neogul.whynago.admin.implement.dto;

import com.neogul.whynago.admin.domain.DashboardAlertType;
import java.time.LocalDate;

// 판정에 쓴 원시 값만 담는다. 알림 종류가 늘어 타입별 필드가 많아지면 sealed interface 분리를 검토한다.
public record DashboardAlert(DashboardAlertType type, LocalDate interviewDate) {

    public static DashboardAlert dailyInterviewNotPinned(LocalDate interviewDate) {
        return new DashboardAlert(DashboardAlertType.DAILY_INTERVIEW_NOT_PINNED, interviewDate);
    }
}
