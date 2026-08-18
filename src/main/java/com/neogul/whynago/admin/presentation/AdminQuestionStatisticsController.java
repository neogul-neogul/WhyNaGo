package com.neogul.whynago.admin.presentation;

import com.neogul.whynago.admin.presentation.dto.MultipleChoiceStatisticsResponse;
import com.neogul.whynago.admin.service.AdminQuestionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 통계는 요청자와 무관하고 권한은 AdminInterceptor가 이미 확인했으므로 로그인 사용자를 받지 않는다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/questions")
public class AdminQuestionStatisticsController {

    private final AdminQuestionStatisticsService adminQuestionStatisticsService;

    @GetMapping("/{questionId}/statistics")
    public ResponseEntity<MultipleChoiceStatisticsResponse> findMultipleChoiceStatistics(
            @PathVariable Long questionId
    ) {
        return ResponseEntity.ok(MultipleChoiceStatisticsResponse.from(
                adminQuestionStatisticsService.readMultipleChoiceStatistics(questionId)));
    }
}
