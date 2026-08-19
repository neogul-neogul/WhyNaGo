package com.neogul.whynago.mastery.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.mastery.presentation.dto.MasteryResponse;
import com.neogul.whynago.mastery.service.MasteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mastery")
public class MasteryController {

    private final MasteryService masteryService;

    @GetMapping
    public ResponseEntity<MasteryResponse> getMastery(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(MasteryResponse.from(masteryService.getMastery(authContext.id())));
    }
}
