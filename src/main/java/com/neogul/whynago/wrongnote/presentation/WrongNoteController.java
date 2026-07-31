package com.neogul.whynago.wrongnote.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.wrongnote.presentation.dto.UpdateWrongNoteBookmarkRequest;
import com.neogul.whynago.wrongnote.presentation.dto.WrongNoteBookmarkResponse;
import com.neogul.whynago.wrongnote.presentation.dto.WrongNoteDetailResponse;
import com.neogul.whynago.wrongnote.presentation.dto.WrongNoteSummaryResponse;
import com.neogul.whynago.wrongnote.service.WrongNoteService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wrong-notes")
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    @GetMapping
    public ResponseEntity<List<WrongNoteSummaryResponse>> findAll(
            @LoginUser AuthContext authContext,
            @RequestParam(required = false) Boolean bookmarked
    ) {
        List<WrongNoteSummaryResponse> responses = wrongNoteService.findAll(authContext.id(), bookmarked).stream()
                .map(WrongNoteSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{wrongNoteId}")
    public ResponseEntity<WrongNoteDetailResponse> findDetail(
            @LoginUser AuthContext authContext,
            @PathVariable Long wrongNoteId
    ) {
        WrongNoteDetailResponse response = WrongNoteDetailResponse.from(
                wrongNoteService.findDetail(authContext.id(), wrongNoteId));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{wrongNoteId}/bookmark")
    public ResponseEntity<WrongNoteBookmarkResponse> updateBookmark(
            @LoginUser AuthContext authContext,
            @PathVariable Long wrongNoteId,
            @Valid @RequestBody UpdateWrongNoteBookmarkRequest request
    ) {
        WrongNoteBookmarkResponse response = WrongNoteBookmarkResponse.from(
                wrongNoteService.updateBookmark(authContext.id(), wrongNoteId, request.bookmarked()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{wrongNoteId}")
    public ResponseEntity<Void> delete(
            @LoginUser AuthContext authContext,
            @PathVariable Long wrongNoteId
    ) {
        wrongNoteService.delete(authContext.id(), wrongNoteId);
        return ResponseEntity.noContent().build();
    }
}
