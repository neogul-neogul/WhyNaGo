package com.neogul.whynago.question.infra.ai;

import java.util.List;

public interface EssayAiClient {

    GradeAndFollowupResult gradeAndGenerateFollowup(List<EssayTurn> thread, boolean generateFollowup);
}
