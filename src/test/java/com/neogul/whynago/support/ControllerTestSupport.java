package com.neogul.whynago.support;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.implement.JwtProvider;
import com.neogul.whynago.auth.presentation.AuthController;
import com.neogul.whynago.auth.presentation.interceptor.TokenExtractor;
import com.neogul.whynago.auth.service.AuthService;
import com.neogul.whynago.question.presentation.QuestionController;
import com.neogul.whynago.question.service.EssayAnswerService;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.solvedsession.presentation.SolvedSessionController;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        AuthController.class,
        QuestionController.class,
        SolvedSessionController.class
})
@Import({JwtProvider.class, TokenExtractor.class})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtProvider jwtProvider;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected QuestionService questionService;

    @MockitoBean
    protected EssayAnswerService essayAnswerService;

    @MockitoBean
    protected SolvedSessionService solvedSessionService;

    @BeforeEach
    void setUpMockMvc() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    protected String bearerToken(Long userId) {
        return "Bearer " + jwtProvider.createAccessToken(new JwtClaim(userId));
    }
}
