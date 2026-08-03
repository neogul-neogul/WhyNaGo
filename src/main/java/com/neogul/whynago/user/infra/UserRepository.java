package com.neogul.whynago.user.infra;

import com.neogul.whynago.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailValue(String email);

    boolean existsByEmailValue(String email);

    boolean existsByNickname(String nickname);

    boolean existsByEmailValueAndIdNot(String email, Long id);

    boolean existsByNicknameAndIdNot(String nickname, Long id);
}
