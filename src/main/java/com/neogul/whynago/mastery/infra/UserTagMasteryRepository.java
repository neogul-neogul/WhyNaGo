package com.neogul.whynago.mastery.infra;

import com.neogul.whynago.mastery.domain.UserTagMastery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTagMasteryRepository extends JpaRepository<UserTagMastery, Long> {

    Optional<UserTagMastery> findByUserIdAndTagId(Long userId, Long tagId);

    List<UserTagMastery> findByUserId(Long userId);
}
