package com.neogul.whynago.question.domain;

import java.util.List;

// 꼬리질문 가드레일. 정답표가 아니라 "어디까지 더 들어가도 되는가"의 범위다.
// allowed는 한 단계 더 파고들어도 되는 개념, forbidden은 신입 수준을 넘어서는 영역이다.
public record FollowupScope(List<String> allowed, List<String> forbidden) {

    public List<String> allowed() {
        return allowed == null ? List.of() : allowed;
    }

    public List<String> forbidden() {
        return forbidden == null ? List.of() : forbidden;
    }

    public boolean isEmpty() {
        return allowed().isEmpty() && forbidden().isEmpty();
    }
}
