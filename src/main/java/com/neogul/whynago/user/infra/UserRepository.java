package com.neogul.whynago.user.infra;

import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailValue(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByEmailValue(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    // createdAt이 null인 회원(추적 시작 전 가입)은 between 조건에 걸리지 않아 자연히 제외된다.
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    // 관리자 회원 목록. createdAt은 추적 이전 가입자가 null이라 정렬 키로 쓸 수 없어 id 역순으로 둔다.
    @Query(value = """
            select u
            from User u
            where (:keyword is null
                   or lower(u.nickname) like lower(concat('%', :keyword, '%'))
                   or lower(u.email.value) like lower(concat('%', :keyword, '%')))
            order by u.id desc
            """,
            countQuery = """
            select count(u)
            from User u
            where (:keyword is null
                   or lower(u.nickname) like lower(concat('%', :keyword, '%'))
                   or lower(u.email.value) like lower(concat('%', :keyword, '%')))
            """)
    Page<User> findUsers(@Param("keyword") String keyword, Pageable pageable);
}
