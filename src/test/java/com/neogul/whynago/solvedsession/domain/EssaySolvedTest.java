package com.neogul.whynago.solvedsession.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EssaySolvedTest {

    @Test
    @DisplayName("본질문 문항을 생성하면 questionId를 포함해 필드가 채워진다.")
    void createMain() {
        LocalDateTime solvedAt = LocalDateTime.now();

        EssaySolved essaySolved = EssaySolved.create(
                1L,
                10L,
                ItemType.MAIN,
                1,
                100L,
                "트랜잭션 격리 수준을 설명하라.",
                "격리 수준은 4단계로 나뉜다.",
                "핵심은 짚었으나 이상 현상 설명이 부족합니다.",
                "READ UNCOMMITTED부터 SERIALIZABLE까지...",
                true,
                null,
                solvedAt
        );

        assertThat(essaySolved.getSolvedSessionId()).isEqualTo(1L);
        assertThat(essaySolved.getUserId()).isEqualTo(10L);
        assertThat(essaySolved.getType()).isEqualTo(ItemType.MAIN);
        assertThat(essaySolved.getSequence()).isOne();
        assertThat(essaySolved.getQuestionId()).isEqualTo(100L);
        assertThat(essaySolved.getQuestionText()).isEqualTo("트랜잭션 격리 수준을 설명하라.");
        assertThat(essaySolved.getUserAnswer()).isEqualTo("격리 수준은 4단계로 나뉜다.");
        assertThat(essaySolved.isCorrect()).isTrue();
        assertThat(essaySolved.getSolvedAt()).isEqualTo(solvedAt);
    }

    @Test
    @DisplayName("꼬리질문 문항은 questionId 없이 생성된다.")
    void createFollowup() {
        EssaySolved essaySolved = EssaySolved.create(
                1L,
                10L,
                ItemType.FOLLOWUP,
                2,
                null,
                "격리 수준별 이상 현상을 설명하라.",
                "팬텀 리드는 SERIALIZABLE에서 막힌다.",
                "정확합니다.",
                "이상 현상은 Dirty Read, Non-Repeatable Read, Phantom Read...",
                false,
                null,
                LocalDateTime.now()
        );

        assertThat(essaySolved.getType()).isEqualTo(ItemType.FOLLOWUP);
        assertThat(essaySolved.getQuestionId()).isNull();
        assertThat(essaySolved.isCorrect()).isFalse();
    }
}
