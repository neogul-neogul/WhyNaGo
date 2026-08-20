package com.neogul.whynago.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.support.RepositoryTestSupport;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.fixture.UserFixture;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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

    @DisplayName("가입 시각이 기간에 속한 회원 수만 센다.")
    @Test
    void countByCreatedAtBetween() {
        // given
        em.persist(UserFixture.user()
                .email("today1@example.com").nickname("today1")
                .createdAt(LocalDateTime.of(2026, 8, 19, 0, 0))
                .build());
        em.persist(UserFixture.user()
                .email("today2@example.com").nickname("today2")
                .createdAt(LocalDateTime.of(2026, 8, 19, 23, 59, 59))
                .build());
        em.persist(UserFixture.user()
                .email("yesterday@example.com").nickname("yester")
                .createdAt(LocalDateTime.of(2026, 8, 18, 23, 59, 59))
                .build());
        em.flush();

        // when
        long count = userRepository.countByCreatedAtBetween(
                LocalDateTime.of(2026, 8, 19, 0, 0),
                LocalDateTime.of(2026, 8, 19, 23, 59, 59, 999_999_999)
        );

        // then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("가입 시각이 없는 회원은 기간 집계에서 제외된다.")
    @Test
    void countByCreatedAtBetween_createdAtIsNull() {
        // given
        em.persist(UserFixture.user()
                .email("legacy@example.com").nickname("legacy")
                .createdAtNotTracked()
                .build());
        em.flush();

        // when
        long count = userRepository.countByCreatedAtBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.of(2100, 1, 1, 0, 0)
        );

        // then
        assertThat(count).isZero();
    }

    @DisplayName("닉네임 일부로 회원을 검색한다.")
    @Test
    void findUsers_byNickname() {
        // given
        persistUser("devhoon@example.com", "devhoon");
        persistUser("mina@example.com", "minakim");

        // when
        Page<User> found = userRepository.findUsers("hoon", PageRequest.of(0, 10));

        // then
        assertThat(found.getContent()).extracting(User::getNickname).containsExactly("devhoon");
    }

    @DisplayName("이메일 일부로 회원을 검색한다.")
    @Test
    void findUsers_byEmail() {
        // given
        persistUser("devhoon@gmail.com", "devhoon");
        persistUser("minakim@naver.com", "minakim");

        // when
        Page<User> found = userRepository.findUsers("naver", PageRequest.of(0, 10));

        // then
        assertThat(found.getContent()).extracting(User::getNickname).containsExactly("minakim");
    }

    @DisplayName("검색어의 대소문자는 구분하지 않는다.")
    @Test
    void findUsers_ignoresCase() {
        // given
        persistUser("devhoon@example.com", "DevHoon");

        // when
        Page<User> found = userRepository.findUsers("DEVHOON", PageRequest.of(0, 10));

        // then
        assertThat(found.getContent()).hasSize(1);
    }

    @DisplayName("검색어가 없으면 전체 회원을 가입 역순으로 조회한다.")
    @Test
    void findUsers_noKeyword() {
        // given
        User first = persistUser("first@example.com", "firstus");
        User second = persistUser("second@example.com", "second");
        User third = persistUser("third@example.com", "thirdus");

        // when
        Page<User> found = userRepository.findUsers(null, PageRequest.of(0, 10));

        // then
        assertThat(found.getTotalElements()).isEqualTo(3);
        assertThat(found.getContent()).extracting(User::getId)
                .containsExactly(third.getId(), second.getId(), first.getId());
    }

    @DisplayName("전체 회원 수는 유지하면서 요청한 페이지만 조회한다.")
    @Test
    void findUsers_paged() {
        // given
        persistUser("first@example.com", "firstus");
        User second = persistUser("second@example.com", "second");
        persistUser("third@example.com", "thirdus");

        // when
        Page<User> found = userRepository.findUsers(null, PageRequest.of(1, 1));

        // then
        assertThat(found.getTotalElements()).isEqualTo(3);
        assertThat(found.getContent()).extracting(User::getId).containsExactly(second.getId());
    }

    private User persistUser(String email, String nickname) {
        return em.persistAndFlush(UserFixture.user().email(email).nickname(nickname).build());
    }
}