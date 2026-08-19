package com.neogul.whynago.solvedsession.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SolvedMultipleChoiceTest {

    @Test
    @DisplayName("문항 소요 시간을 함께 저장한다.")
    void create_withElapsedSeconds() {
        // when
        SolvedMultipleChoice solved = create(75);

        // then
        assertThat(solved.getElapsedSeconds()).isEqualTo(75);
    }

    @Test
    @DisplayName("소요 시간이 1시간을 넘으면 자리를 비운 것으로 보고 저장하지 않는다.")
    void create_elapsedSecondsOverLimit() {
        // when
        SolvedMultipleChoice solved = create(3601);

        // then
        assertThat(solved.getElapsedSeconds()).isNull();
    }

    @Test
    @DisplayName("소요 시간이 1시간이면 그대로 저장한다.")
    void create_elapsedSecondsAtLimit() {
        // when
        SolvedMultipleChoice solved = create(3600);

        // then
        assertThat(solved.getElapsedSeconds()).isEqualTo(3600);
    }

    @Test
    @DisplayName("소요 시간을 수집하지 않은 풀이는 소요 시간이 없다.")
    void create_withoutElapsedSeconds() {
        // when
        SolvedMultipleChoice solved = SolvedMultipleChoice.create(
                1L,
                10L,
                100L,
                ItemType.MAIN,
                1,
                1L,
                1L,
                true,
                LocalDateTime.now()
        );

        // then
        assertThat(solved.getElapsedSeconds()).isNull();
    }

    private SolvedMultipleChoice create(Integer elapsedSeconds) {
        return SolvedMultipleChoice.create(
                1L,
                10L,
                100L,
                ItemType.MAIN,
                1,
                1L,
                1L,
                true,
                LocalDateTime.now(),
                elapsedSeconds
        );
    }
}
