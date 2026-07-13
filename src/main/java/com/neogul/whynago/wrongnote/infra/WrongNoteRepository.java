package com.neogul.whynago.wrongnote.infra;

import com.neogul.whynago.wrongnote.domain.WrongNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {

    boolean existsByUserIdAndSolvedSessionId(Long userId, Long solvedSessionId);
}
