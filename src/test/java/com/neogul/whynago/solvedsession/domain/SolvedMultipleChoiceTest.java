package com.neogul.whynago.solvedsession.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 정규화 규칙 자체의 경계값은 ElapsedSecondsPolicyTest가 전수로 본다.
// 여기서는 엔티티가 그 정책을 거쳐 값을 저장하는지만 확인한다.
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
    @DisplayName("소요 시간이 상한을 넘으면 상한으로 잘라 저장한다.")
    void create_elapsedSecondsOverLimit() {
        // when
        SolvedMultipleChoice solved = create(ElapsedSecondsPolicy.MAX_SECONDS + 1);

        // then
        assertThat(solved.getElapsedSeconds()).isEqualTo(ElapsedSecondsPolicy.MAX_SECONDS);
    }

    @Test
    @DisplayName("소요 시간이 상한과 같으면 그대로 저장한다.")
    void create_elapsedSecondsAtLimit() {
        // when
        SolvedMultipleChoice solved = create(ElapsedSecondsPolicy.MAX_SECONDS);

        // then
        assertThat(solved.getElapsedSeconds()).isEqualTo(ElapsedSecondsPolicy.MAX_SECONDS);
    }

    @Test
    @DisplayName("0초는 측정 실패로 보고 저장하지 않는다.")
    void create_zeroElapsedSeconds() {
        // when
        SolvedMultipleChoice solved = create(0);

        // then
        assertThat(solved.getElapsedSeconds()).isNull();
    }

    @Test
    @DisplayName("소요 시간을 수집하지 않은 풀이는 소요 시간이 없다.")
    void create_withoutElapsedSeconds() {
        // when
        SolvedMultipleChoice solved = create(null);

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
                elapsedSeconds,
                LocalDateTime.now()
        );
    }
}
