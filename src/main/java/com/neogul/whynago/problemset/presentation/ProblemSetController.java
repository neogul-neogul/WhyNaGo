package com.neogul.whynago.problemset.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.problemset.presentation.dto.CreateProblemSetRequest;
import com.neogul.whynago.problemset.presentation.dto.CreateProblemSetResponse;
import com.neogul.whynago.problemset.presentation.dto.ProblemSetDetailResponse;
import com.neogul.whynago.problemset.presentation.dto.ProblemSetMembershipResponse;
import com.neogul.whynago.problemset.presentation.dto.ProblemSetSummaryResponse;
import com.neogul.whynago.problemset.service.ProblemSetService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem-sets")
public class ProblemSetController {

    private final ProblemSetService problemSetService;

    @PostMapping
    public ResponseEntity<CreateProblemSetResponse> create(
            @LoginUser AuthContext authContext,
            @Valid @RequestBody CreateProblemSetRequest request
    ) {
        CreateProblemSetResponse response = CreateProblemSetResponse.from(
                problemSetService.create(request.toCommand(authContext.id())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProblemSetSummaryResponse>> findAll(@LoginUser AuthContext authContext) {
        List<ProblemSetSummaryResponse> responses = problemSetService.findAll(authContext.id()).stream()
                .map(ProblemSetSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/membership")
    public ResponseEntity<List<ProblemSetMembershipResponse>> findMembership(
            @LoginUser AuthContext authContext,
            @RequestParam Long questionId
    ) {
        List<ProblemSetMembershipResponse> responses = problemSetService.findMembership(authContext.id(), questionId)
                .stream()
                .map(ProblemSetMembershipResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{problemSetId}")
    public ResponseEntity<ProblemSetDetailResponse> findDetail(
            @LoginUser AuthContext authContext,
            @PathVariable Long problemSetId
    ) {
        ProblemSetDetailResponse response = ProblemSetDetailResponse.from(
                problemSetService.findDetail(authContext.id(), problemSetId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{problemSetId}/items/{questionId}")
    public ResponseEntity<Void> addItem(
            @LoginUser AuthContext authContext,
            @PathVariable Long problemSetId,
            @PathVariable Long questionId
    ) {
        problemSetService.addItem(authContext.id(), problemSetId, questionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{problemSetId}/items/{questionId}")
    public ResponseEntity<Void> removeItem(
            @LoginUser AuthContext authContext,
            @PathVariable Long problemSetId,
            @PathVariable Long questionId
    ) {
        problemSetService.removeItem(authContext.id(), problemSetId, questionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{problemSetId}")
    public ResponseEntity<Void> delete(
            @LoginUser AuthContext authContext,
            @PathVariable Long problemSetId
    ) {
        problemSetService.delete(authContext.id(), problemSetId);
        return ResponseEntity.noContent().build();
    }
}
