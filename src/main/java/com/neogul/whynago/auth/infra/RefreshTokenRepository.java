package com.neogul.whynago.auth.infra;

import com.neogul.whynago.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 삭제된 행 수를 반환한다. 0이면 이미 폐기된(재사용된) 토큰이다.
     *
     * <p>파생 쿼리(deleteByTokenHash 자동 생성)는 SELECT 후 엔티티를 개별 remove 하므로,
     * 같은 토큰으로 동시 요청이 오면 두 번째 트랜잭션이 이미 사라진 행을 지우려다
     * StaleStateException을 던진다. 벌크 삭제는 DB가 영향 행 수를 직접 반환해
     * 0건을 그대로 판정할 수 있다.
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying(flushAutomatically = true)
    @Query("delete from RefreshToken r where r.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}