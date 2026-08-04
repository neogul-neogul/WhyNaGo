package com.neogul.whynago.learningrecord.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.learningrecord.presentation.dto.DailyRecordCountResponse;
import com.neogul.whynago.learningrecord.presentation.dto.RecentRecordResponse;
import com.neogul.whynago.learningrecord.presentation.dto.StreakResponse;
import com.neogul.whynago.learningrecord.service.LearningRecordService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learning-records")
public class LearningRecordController {

    private final LearningRecordService learningRecordService;

    @GetMapping("/recent")
    public ResponseEntity<List<RecentRecordResponse>> findRecent(
            @LoginUser AuthContext authContext,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<RecentRecordResponse> responses = learningRecordService.findRecent(authContext.id(), size).stream()
                .map(RecentRecordResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(StreakResponse.from(learningRecordService.getStreak(authContext.id())));
    }

    @GetMapping("/daily-counts")
    public ResponseEntity<List<DailyRecordCountResponse>> findDailyCounts(
            @LoginUser AuthContext authContext,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        List<DailyRecordCountResponse> responses = learningRecordService.findDailyCounts(authContext.id(), from, to).stream()
                .map(DailyRecordCountResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}