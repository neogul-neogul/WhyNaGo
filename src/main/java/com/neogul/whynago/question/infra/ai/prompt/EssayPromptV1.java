package com.neogul.whynago.question.infra.ai.prompt;

import com.neogul.whynago.question.domain.EssayGradingMode;

public class EssayPromptV1 implements EssayPrompt {

    private static final String VERSION = "v1";

    private static final String COMMON_GRADING_RULES = """
            한국어로 작성하고, 정답을 단정하기보다 보완할 점 중심으로 feedback과 modelAnswer를 채워라.
            score는 0부터 10 사이 정수로, 답변의 정확성과 완성도를 평가해 매겨라.
            이전 문답이 대화 이력으로 주어지면 그 맥락을 활용하되, 항상 마지막에 주어진 '채점 대상' 답변만 평가하라.
            """;

    private static final String INTERVIEW_SYSTEM_PROMPT = """
            너는 개발자 채용 기술 면접관이다. 지원자의 답변을 평가한다.
            """ + COMMON_GRADING_RULES;

    private static final String PRACTICE_SYSTEM_PROMPT = """
            너는 개발자 학습을 돕는 기술 튜터다. 학습자가 스스로 풀어본 문제의 답변을 평가한다.
            채용 상황이 아니므로 '지원자', '면접' 같은 표현을 쓰지 마라.
            feedback은 두 단계로 쓴다. 먼저 답변에서 빠졌거나 틀린 내용을 짚고,
            이어서 그 빈틈을 메우려면 무엇을 공부해야 하는지 개념·기술의 이름을 구체적으로 제시하라.
            '조금 더 공부해 보세요' 같은 막연한 권유는 쓰지 말고 학습할 대상을 이름으로 못 박아라.
            칭찬·격려·위로 표현은 넣지 말고 군더더기 없이 사실만 짚어라.
            """ + COMMON_GRADING_RULES;

    private static final String GENERATE_FOLLOWUP_INSTRUCTION = """
            또한 이 문답 흐름에 이어서 이해도를 더 깊이 확인할 꼬리질문 한 개를 한국어로 생성하라.
            새로운 주제로 벗어나지 말고 직전 답변을 파고들어 followupQuestion에 담아라.
            꼬리질문은 답변자가 직접 언급한 개념이나 이전 질문에 나온 개념을 기준으로 딱 한 단계만 더 깊게 들어가라.
            답변과 이전 질문에 나오지 않은 새로운 전문 개념·용어를 끌어와 묻지 마라.
            단, 답변자가 스스로 깊은 수준까지 설명했다면 그 답변 수준을 기준으로 삼아 거기서 한 단계만 더 들어가라.""";

    private static final String UNKNOWN_TERM_FOLLOWUP_RULE = """

            직전 답변에서 모른다고 밝힌 용어는 꼬리질문에 다시 등장시키지 마라.
            '~로 넘어가기 전에', '~를 이해하려면' 처럼 그 용어를 언급하며 우회하는 문장도 금지한다.
            그 용어를 빼고, 답변자가 알 만한 더 기초적인 인접 개념만으로 질문을 새로 만들어라.""";

    private static final String NO_FOLLOWUP_INSTRUCTION =
            "이번 턴에서는 꼬리질문을 생성하지 말고 followupQuestion은 null로 두어라.";

    private static final String USER_PROMPT_TEMPLATE = """
            [채점 대상]
            질문: %s
            답변: %s

            %s
            """;

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public String systemPrompt(EssayGradingMode mode) {
        return switch (mode) {
            case INTERVIEW -> INTERVIEW_SYSTEM_PROMPT;
            case PRACTICE -> PRACTICE_SYSTEM_PROMPT;
        };
    }

    @Override
    public String userPrompt(EssayGradingMode mode, String question, String answer, boolean generateFollowup) {
        String followupInstruction = generateFollowup ? followupInstructionOf(mode) : NO_FOLLOWUP_INSTRUCTION;
        return USER_PROMPT_TEMPLATE.formatted(question, answer, followupInstruction);
    }

    private static String followupInstructionOf(EssayGradingMode mode) {
        return switch (mode) {
            case INTERVIEW -> GENERATE_FOLLOWUP_INSTRUCTION + UNKNOWN_TERM_FOLLOWUP_RULE;
            case PRACTICE -> GENERATE_FOLLOWUP_INSTRUCTION;
        };
    }
}
