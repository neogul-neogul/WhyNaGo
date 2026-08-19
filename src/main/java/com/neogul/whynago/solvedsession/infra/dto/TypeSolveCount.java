package com.neogul.whynago.solvedsession.infra.dto;

import com.neogul.whynago.question.domain.QuestionType;

public interface TypeSolveCount {

    QuestionType getType();

    long getQuestionCount();
}
