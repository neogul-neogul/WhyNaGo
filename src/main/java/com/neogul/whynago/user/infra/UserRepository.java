package com.neogul.whynago.user.infra;

import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailValue(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByEmailValue(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    // createdAt이 null인 회원(추적 시작 전 가입)은 between 조건에 걸리지 않아 자연히 제외된다.
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
