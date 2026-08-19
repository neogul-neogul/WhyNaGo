package com.neogul.whynago.solvedsession.scheduler;

import com.neogul.whynago.solvedsession.service.QuestionStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionStatScheduler {

    private final QuestionStatService questionStatService;

    // 사용자 활동이 가장 적은 새벽에 전량 재집계한다.
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    public void refreshQuestionStats() {
        questionStatService.refreshAll();
    }
}
