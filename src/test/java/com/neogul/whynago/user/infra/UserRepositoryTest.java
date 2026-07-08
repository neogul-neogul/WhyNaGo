package com.neogul.whynago.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.support.RepositoryTestSupport;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.fixture.UserFixture;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이메일 값 객체 경로로 사용자를 조회한다.")
    @Test
    void findByEmailValue() {
        // given
        User user = UserFixture.user().email("member@example.com").build();
        em.persistAndFlush(user);
        em.clear();

        // when
        Optional<User> found = userRepository.findByEmailValue("member@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail().getValue()).isEqualTo("member@example.com");
    }

    @DisplayName("존재하지 않는 이메일로 조회하면 빈 결과를 반환한다.")
    @Test
    void findByEmailValue_notFound() {
        // when
        Optional<User> found = userRepository.findByEmailValue("none@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @DisplayName("등록된 이메일이면 존재 여부가 true다.")
    @Test
    void existsByEmailValue() {
        // given
        User user = UserFixture.user().email("member@example.com").build();
        em.persistAndFlush(user);

        // when & then
        assertThat(userRepository.existsByEmailValue("member@example.com")).isTrue();
    }

    @DisplayName("등록되지 않은 이메일이면 존재 여부가 false다.")
    @Test
    void existsByEmailValue_notRegistered() {
        // when & then
        assertThat(userRepository.existsByEmailValue("none@example.com")).isFalse();
    }

    @DisplayName("등록된 닉네임이면 존재 여부가 true다.")
    @Test
    void existsByNickname() {
        // given
        User user = UserFixture.user().nickname("hello").build();
        em.persistAndFlush(user);

        // when & then
        assertThat(userRepository.existsByNickname("hello")).isTrue();
    }

    @DisplayName("등록되지 않은 닉네임이면 존재 여부가 false다.")
    @Test
    void existsByNickname_notRegistered() {
        // when & then
        assertThat(userRepository.existsByNickname("none")).isFalse();
    }
}