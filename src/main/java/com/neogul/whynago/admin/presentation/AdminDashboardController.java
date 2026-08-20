package com.neogul.whynago.admin.presentation;

import com.neogul.whynago.admin.presentation.dto.DashboardResponse;
import com.neogul.whynago.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 조회는 요청자와 무관하고 권한은 AdminInterceptor가 이미 확인했으므로 로그인 사용자를 받지 않는다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> findDashboard() {
        return ResponseEntity.ok(DashboardResponse.from(adminDashboardService.readDashboard()));
    }
}
