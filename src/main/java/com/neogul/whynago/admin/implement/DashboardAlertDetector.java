package com.neogul.whynago.admin.implement;

import com.neogul.whynago.admin.implement.dto.DashboardAlert;
import com.neogul.whynago.interview.implement.DailyInterviewQuestionReader;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardAlertDetector {

    private final DailyInterviewQuestionReader dailyInterviewQuestionReader;

    /**
     * 오늘 문항이 고정되지 않았다는 사실만 알린다.
     * 고정은 첫 사용자가 면접을 시작할 때 이뤄지므로 이른 시각의 미고정은 정상 상태다.
     * 따라서 서버가 기준 시각을 정해 경고로 격상하지 않고, 노출 여부는 화면이 판단한다.
     */
    public List<DashboardAlert> detect(LocalDate today) {
        if (dailyInterviewQuestionReader.findByDate(today).isPresent()) {
            return List.of();
        }
        return List.of(DashboardAlert.dailyInterviewNotPinned(today));
    }
}
