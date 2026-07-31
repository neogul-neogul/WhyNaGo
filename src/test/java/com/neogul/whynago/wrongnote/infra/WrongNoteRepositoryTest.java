package com.neogul.whynago.wrongnote.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.support.RepositoryTestSupport;
import com.neogul.whynago.wrongnote.domain.WrongNote;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WrongNoteRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private WrongNoteRepository wrongNoteRepository;

    @Test
    @DisplayName("소유자의 오답노트를 ID로 조회한다.")
    void findByIdAndUserId() {
        WrongNote note = wrongNoteRepository.save(WrongNote.create(1L, 100L));

        Optional<WrongNote> found = wrongNoteRepository.findByIdAndUserId(note.getId(), 1L);

        assertThat(found).get().extracting(WrongNote::getSolvedSessionId).isEqualTo(100L);
    }

    @Test
    @DisplayName("다른 사용자 소유의 오답노트는 조회되지 않는다.")
    void findByIdAndUserId_notOwner() {
        WrongNote note = wrongNoteRepository.save(WrongNote.create(1L, 100L));

        Optional<WrongNote> found = wrongNoteRepository.findByIdAndUserId(note.getId(), 2L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("사용자의 오답노트 전체를 최신순으로 조회한다.")
    void findByUserIdOrderByIdDesc() {
        WrongNote first = wrongNoteRepository.save(WrongNote.create(1L, 100L));
        WrongNote second = wrongNoteRepository.save(WrongNote.create(1L, 101L));
        wrongNoteRepository.save(WrongNote.create(2L, 102L));

        List<WrongNote> result = wrongNoteRepository.findByUserIdOrderByIdDesc(1L);

        assertThat(result).extracting(WrongNote::getId).containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("북마크한 오답노트만 필터링해 조회한다.")
    void findByUserIdAndIsBookmarkedOrderByIdDesc() {
        WrongNote bookmarked = WrongNote.create(1L, 100L);
        bookmarked.updateBookmark(true);
        wrongNoteRepository.save(bookmarked);
        wrongNoteRepository.save(WrongNote.create(1L, 101L));

        List<WrongNote> result = wrongNoteRepository.findByUserIdAndIsBookmarkedOrderByIdDesc(1L, true);

        assertThat(result).extracting(WrongNote::getSolvedSessionId).containsExactly(100L);
    }
}
