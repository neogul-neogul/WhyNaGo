package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.FollowupScope;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.RubricCriterion;
import java.util.List;

// 파이프라인이 보장하는 불변식(항목 3~5개, 배점 합 정확히 10)을 지킨 루브릭이다.
public final class RubricFixture {

    private RubricFixture() {
    }

    // 배점 3 / 3 / 4로 합이 10인 3항목 루브릭이다.
    public static Rubric threeCriteria() {
        return new Rubric(
                List.of(
                        new RubricCriterion("TCP는 신뢰성 있는 데이터 전송이 필요한 경우에 사용된다.", 3),
                        new RubricCriterion("UDP는 실시간 통신이나 저지연이 중요한 서비스에 사용된다.", 3),
                        new RubricCriterion("TCP의 흐름 제어와 혼잡 제어가 처리 지연을 유발한다.", 4)),
                new FollowupScope(List.of("흐름 제어", "혼잡 제어"), List.of("TCP/IP 계층 구조")));
    }

    public static Rubric withoutFollowupScope() {
        return new Rubric(threeCriteria().criteria(), null);
    }
}
