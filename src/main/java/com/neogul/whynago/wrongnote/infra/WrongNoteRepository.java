package com.neogul.whynago.wrongnote.infra;

import com.neogul.whynago.wrongnote.domain.WrongNote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {

    boolean existsByUserIdAndSolvedSessionId(Long userId, Long solvedSessionId);

    Optional<WrongNote> findByIdAndUserId(Long id, Long userId);

    List<WrongNote> findByUserIdOrderByIdDesc(Long userId);

    List<WrongNote> findByUserIdAndIsBookmarkedOrderByIdDesc(Long userId, boolean isBookmarked);
}
