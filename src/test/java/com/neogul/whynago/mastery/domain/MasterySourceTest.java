package com.neogul.whynago.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MasterySourceTest {

    @Test
    @DisplayName("꼬리질문 판정만 현재값 갱신 대상에서 제외된다.")
    void isFollowup() {
        assertThat(MasterySource.AI_ESSAY_FOLLOWUP.isFollowup()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = MasterySource.class, names = {"AI_ESSAY", "RULE_CHOICE"})
    @DisplayName("본질문·객관식 판정은 현재값을 갱신한다.")
    void isFollowup_false(MasterySource source) {
        assertThat(source.isFollowup()).isFalse();
    }
}
