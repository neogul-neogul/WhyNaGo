package com.neogul.whynago.wrongnote.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.wrongnote.domain.WrongNote;
import com.neogul.whynago.wrongnote.exception.WrongNoteErrorCode;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WrongNoteReader {

    private final WrongNoteRepository wrongNoteRepository;

    public WrongNote read(Long userId, Long wrongNoteId) {
        return wrongNoteRepository.findByIdAndUserId(wrongNoteId, userId)
                .orElseThrow(() -> new BusinessException(WrongNoteErrorCode.WRONG_NOTE_NOT_FOUND));
    }

    public List<WrongNote> readAll(Long userId, Boolean bookmarked) {
        if (bookmarked == null) {
            return wrongNoteRepository.findByUserIdOrderByIdDesc(userId);
        }
        return wrongNoteRepository.findByUserIdAndIsBookmarkedOrderByIdDesc(userId, bookmarked);
    }
}
