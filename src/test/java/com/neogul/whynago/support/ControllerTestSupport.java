package com.neogul.whynago.support;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.implement.JwtProvider;
import com.neogul.whynago.auth.presentation.AuthController;
import com.neogul.whynago.auth.presentation.interceptor.TokenExtractor;
import com.neogul.whynago.auth.service.AuthService;
import com.neogul.whynago.learningrecord.presentation.LearningRecordController;
import com.neogul.whynago.learningrecord.service.LearningRecordService;
import com.neogul.whynago.question.presentation.QuestionController;
import com.neogul.whynago.question.service.EssayAnswerService;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.solvedsession.presentation.EssaySolvedSessionController;
import com.neogul.whynago.solvedsession.presentation.SolvedSessionController;
import com.neogul.whynago.solvedsession.service.EssaySolvedSessionService;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import com.neogul.whynago.user.presentation.UserController;
import com.neogul.whynago.user.service.UserService;
import com.neogul.whynago.wrongnote.presentation.WrongNoteController;
import com.neogul.whynago.wrongnote.service.WrongNoteService;
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
        SolvedSessionController.class,
        EssaySolvedSessionController.class,
        WrongNoteController.class,
        LearningRecordController.class,
        UserController.class
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

    @MockitoBean
    protected EssaySolvedSessionService essaySolvedSessionService;

    @MockitoBean
    protected WrongNoteService wrongNoteService;

    @MockitoBean
    protected LearningRecordService learningRecordService;

    @MockitoBean
    protected UserService userService;

    @BeforeEach
    void setUpMockMvc() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    protected String bearerToken(Long userId) {
        return "Bearer " + jwtProvider.createAccessToken(new JwtClaim(userId));
    }
}
