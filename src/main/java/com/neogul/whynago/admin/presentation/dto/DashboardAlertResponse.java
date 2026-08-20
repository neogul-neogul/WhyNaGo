package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.domain.DashboardAlertType;
import com.neogul.whynago.admin.implement.dto.DashboardAlert;
import java.time.LocalDate;

public record DashboardAlertResponse(DashboardAlertType type, LocalDate interviewDate) {

    public static DashboardAlertResponse from(DashboardAlert alert) {
        return new DashboardAlertResponse(alert.type(), alert.interviewDate());
    }
}
