package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.implement.dto.EssayQnA;
import java.util.List;

public record EvaluateEssayAnswerCommand(List<EssayQnA> thread) {
}
