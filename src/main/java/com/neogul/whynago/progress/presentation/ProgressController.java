package com.neogul.whynago.progress.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.progress.presentation.dto.ProgressResponse;
import com.neogul.whynago.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public ResponseEntity<ProgressResponse> getProgress(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(ProgressResponse.from(progressService.getDetail(authContext.id())));
    }
}
