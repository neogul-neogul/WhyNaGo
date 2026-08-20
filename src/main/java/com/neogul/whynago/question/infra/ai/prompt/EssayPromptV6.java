package com.neogul.whynago.question.infra.ai.prompt;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.RubricCriterion;
import com.neogul.whynago.question.domain.SolvingTime;
import java.util.List;
import java.util.stream.IntStream;

// v5에 답변 소요시간을 추가한 버전이다.
//
// v4는 소요시간을 프롬프트에 넣지 않아 숙련도 판정 기준을 전부 "답변 내용"으로만 정의했고, 그래서
// "정확하지만 한참 헤맨 답변"과 "즉시 정확히 쓴 답변"을 구분할 수 없었다. 객관식은 이미 정답 여부 ×
// 평균 대비 소요시간으로 판정하고 있어(mastery의 MasteryPolicy), 두 트랙의 신호가 어긋나 있었다.
//
// v6은 소요시간과 그 문항의 평균 소요시간을 함께 내려 평균 대비 빠름·보통·느림을 알려준다.
// 절대 초만 주면 난이도별 편차를 구분할 수 없어 모델이 임의로 해석한다.
//
// 시간은 mastery 판정과 feedback에만 쓰게 하고 score는 건드리지 못하게 막는다.
// 점수의 시간 가감은 서버가 결정적으로 처리하므로(SolvingTime.scoreAdjustment), 모델이 함께
// 반영하면 같은 신호가 두 번 먹는다.
public class EssayPromptV6 implements EssayPrompt {

    private static final String VERSION = "v6";

    private static final String COMMON_GRADING_RULES = """
            한국어로 작성하고, 정답을 단정하기보다 보완할 점 중심으로 feedback과 modelAnswer를 채워라.
            score는 0부터 10 사이 정수로, 답변의 정확성과 완성도를 평가해 매겨라.
            이전 문답이 대화 이력으로 주어지면 그 맥락을 활용하되, 항상 마지막에 주어진 '채점 대상' 답변만 평가하라.

            feedback은 자연스러운 문장으로 서술하고, '학습할 대상:', '공부할 것:' 같은 고정된 라벨이나
            목록 머리표를 붙이지 마라. 보완할 개념의 이름은 설명 문장 안에 자연스럽게 녹여서 제시하라.
            답변이 정답이면 놓친 세부 개념을 문장 안에서 짚어 보강해 주고,
            오답이거나 모른다고 하면 먼저 알아야 할 핵심 개념을 문장 안에서 이름으로 제시하라.

            답변자가 답을 모른다고 밝히거나 답변이 비어 있다시피 하면, 채점 대상 답변의 실제 수준에 맞춰 처리하라.
            이때 feedback에는 여러 심화 개념을 나열하지 말고, 그 질문을 이해하기 위해 가장 먼저 알아야 할
            핵심 개념 하나에서 많아야 둘까지만 짚어라.
            모른다고 한 답변에 그보다 더 앞선 심화 개념을 다음에 알아야 할 개념으로 제시하지 마라.
            modelAnswer는 그대로 정확히 제시하되, feedback에서 짚는 개념은 답변자의 현재 수준에서
            다음 한 걸음이 될 만한 것으로 한정하라.
            """;

    // v4·v5와 달리 소요시간이 주어지므로 판정 근거에 시간을 다시 들인다.
    // 다만 1차 근거는 여전히 내용이다. 시간만으로 등급을 정하면 빠르게 틀린 답이 MASTERED가 된다.
    private static final String MASTERY_INSTRUCTION = """
            또한 이 답변이 드러낸 이해 수준을 mastery에 아래 여섯 값 중 하나로 판정하라.
            1차 근거는 답변에 실제로 쓰인 내용이고, 소요시간은 그 위에 얹는 보조 근거다.
            답변 길이나 문장 화려함으로 판단하지 마라. 시간이 빠르다는 것만으로 등급을 올리지도 마라.

            MASTERED: 결론과 근거가 모두 정확하고, 묻지 않은 인접 개념까지 스스로 정리해 설명했다. 시간도 평균보다 빠르다.
            SOLID: 결론이 정확하고 필요한 근거를 갖췄다. 사소한 누락은 있어도 이해가 분명하다.
            UNSTABLE: 결론은 맞지만 근거가 틀렸거나 흔들린다. 용어를 혼용하거나 인과를 뒤집어 설명한다.
                      결론과 근거가 정확한데 평균보다 뚜렷하게 오래 걸렸다면 확신이 없는 상태로 보아 여기로 둔다.
            GUESSED: 핵심 용어만 나열했고 근거가 없다. 맞는 말처럼 보이지만 무엇을 왜 그렇게 말했는지가 없다.
                     평균보다 뚜렷하게 빠른데 근거가 비어 있으면 여기에 해당할 가능성이 크다.
            WEAK: 개념을 알고 설명을 시도했지만 결론이 틀렸다. 전형적인 오개념을 그대로 드러낸다.
            NOT_LEARNED: 개념 자체가 없다. 모른다고 밝혔거나, 질문과 무관한 내용을 답했다.

            masteryReason에는 그 판정의 근거를 반드시 채워라. 답변에서 근거가 된 부분을 짚어 두 문장 이내로 쓴다.
            '더 공부가 필요합니다', '이해도가 낮습니다' 같은 일반론은 금지한다.
            무엇을 맞게 말했고 무엇이 빠졌거나 틀렸는지를 구체적으로 쓴다.

            score와 mastery는 서로 모순되지 않아야 한다.
            점수가 높은데 NOT_LEARNED거나, 점수가 낮은데 MASTERED인 판정은 하지 마라.
            """;

    private static final String INTERVIEW_SYSTEM_PROMPT = """
            너는 개발자 채용 기술 면접관이다. 지원자의 답변을 평가한다.
            """ + COMMON_GRADING_RULES + MASTERY_INSTRUCTION;

    private static final String PRACTICE_SYSTEM_PROMPT = """
            너는 개발자 학습을 돕는 기술 튜터다. 학습자가 스스로 풀어본 문제의 답변을 평가한다.
            채용 상황이 아니므로 '지원자', '면접' 같은 표현을 쓰지 마라.
            feedback은 두 단계로 쓴다. 먼저 답변에서 빠졌거나 틀린 내용을 짚고,
            이어서 그 빈틈을 메우려면 무엇을 공부해야 하는지 개념·기술의 이름을 문장 안에서 구체적으로 밝혀라.
            '조금 더 공부해 보세요' 같은 막연한 권유는 쓰지 말고 공부할 개념을 이름으로 분명히 하되,
            '학습할 대상:' 같은 라벨을 붙이지 말고 설명 문장의 일부로 자연스럽게 녹여라.
            칭찬·격려·위로 표현은 넣지 말고 군더더기 없이 사실만 짚어라.
            """ + COMMON_GRADING_RULES + MASTERY_INSTRUCTION;

    private static final String GENERATE_FOLLOWUP_INSTRUCTION = """
            또한 이 문답 흐름에 이어서 이해도를 더 깊이 확인할 꼬리질문 한 개를 한국어로 생성하라.
            새로운 주제로 벗어나지 말고 직전 답변을 파고들어 followupQuestion에 담아라.
            꼬리질문은 답변자가 직접 언급한 개념이나 이전 질문에 나온 개념을 기준으로 딱 한 단계만 더 깊게 들어가라.
            답변과 이전 질문에 나오지 않은 새로운 전문 개념·용어를 끌어와 묻지 마라.
            단, 답변자가 스스로 깊은 수준까지 설명했다면 그 답변 수준을 기준으로 삼아 거기서 한 단계만 더 들어가라.

            답변자가 '모르겠다', '잘 모른다', '기억이 안 난다'처럼 이해 부족을 드러내면
            그 지점에서 더 깊이 들어가지 마라. 모른다고 한 범위를 한 단계 더 파고드는 질문은 금지한다.
            대신 답변자가 이미 이해를 보인 더 기초적인 개념으로 눈높이를 낮추거나,
            같은 깊이에서 답할 수 있을 만한 다른 측면으로 질문을 틀어라.
            답변자가 개념의 일부만 안다고 밝히면, 아는 부분을 딛고 갈 수 있는 가장 가까운 한 걸음만 물어라.

            직전 답변에서 모른다고 밝힌 용어는 꼬리질문에 다시 등장시키지 마라.
            '~로 넘어가기 전에', '~를 이해하려면' 처럼 그 용어를 언급하며 우회하는 문장도 금지한다.
            그 용어를 빼고, 답변자가 알 만한 더 기초적인 인접 개념만으로 질문을 새로 만들어라.

            꼬리질문이 여러 번 이어지더라도, 대화가 누적되며 도달한 깊이가
            신입 개발자가 이해할 수 있는 수준을 넘어서지 않도록 하라.
            직전 답변보다 한 단계 더 들어가되, 그 한 단계가 내부 구현 세부사항이나
            여러 하위 컴포넌트의 계층 구조를 요구하는 영역으로 넘어간다면
            더 들어가지 말고 같은 깊이에서 다른 측면을 물어라.""";

    private static final String NO_FOLLOWUP_INSTRUCTION =
            "이번 턴에서는 꼬리질문을 생성하지 말고 followupQuestion은 null로 두어라.";

    // 루브릭 항목을 1부터 번호로 매겨 내리고, 그 번호로 판정을 되돌려받는다.
    private static final String RUBRIC_INSTRUCTION_TEMPLATE = """
            [채점 기준]
            아래 항목이 이 문항의 채점 기준이다. 각 항목이 답변에 담겼는지 하나씩 판정하라.
            %s

            criteriaResults에 항목 번호마다 정확히 한 개씩, 빠짐없이 담아라.
            - index: 위 항목 번호
            - met: 답변이 그 내용을 담았으면 true, 아니면 false
            - reason: 그 판정의 근거를 한 문장으로. 답변에서 근거가 된 표현을 짚어라.
              met이 false면 무엇이 빠졌는지 쓴다. '언급하지 않았습니다' 같은 반복 대신 어떤 내용이 빠졌는지 밝혀라.

            판정은 표현이 아니라 내용으로 한다. 항목의 문장을 그대로 쓰지 않았어도
            같은 내용을 자기 말로 정확히 설명했다면 met은 true다.
            반대로 용어만 스치고 그 항목의 내용을 설명하지 않았다면 false다.

            score는 met이 true인 항목의 배점 합과 같아야 한다. 다른 값을 쓰지 마라.
            feedback은 met이 false인 항목을 중심으로 쓴다. 충족한 항목을 다시 늘어놓지 마라.
            """;

    // 대화 이력에 본 질문 턴의 루브릭이 남아 있으므로, 꼬리질문 턴에는 비우라고 명시해야 한다.
    private static final String NO_RUBRIC_INSTRUCTION = "criteriaResults는 빈 배열로 두어라.";

    // 시간을 어떻게 읽어야 하는지까지 적는다. 초만 주면 모델이 "오래 고민했으니 성실하다"처럼
    // 반대로 해석하는 경우가 생긴다.
    private static final String SOLVING_TIME_TEMPLATE = """
            [소요시간]
            이 답변을 쓰는 데 %d초가 걸렸다. 이 문항의 평균 소요시간은 %d초이며, 평균 대비 %s.

            소요시간은 mastery 판정과 feedback에만 반영하라.
            - 평균보다 빠르면서 내용까지 정확하면 이미 익숙한 개념으로 본다.
            - 정확하지만 평균보다 오래 걸렸으면 알더라도 확신이 없는 상태로 본다. 결론이 맞아도 MASTERED로 올리지 마라.
            - 평균보다 빠른데 내용이 부실하면 고민 없이 넘긴 것으로 본다.
            느린 것 자체를 성실함으로 읽지 마라. 시간은 이해의 확신을 보는 신호다.

            시간을 근거로 삼았다면 masteryReason에 그 점을 함께 적어라.
            score는 시간으로 조정하지 마라 - 점수의 시간 가감은 서버가 따로 처리한다.
            """;

    private static final String NO_SOLVING_TIME_INSTRUCTION =
            "소요시간은 측정되지 않았다. 시간을 근거로 판정하지 말고 답변 내용만으로 평가하라.";

    private static final String USER_PROMPT_TEMPLATE = """
            [채점 대상]
            질문: %s
            답변: %s

            %s

            %s
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
    public String userPrompt(EssayGradingMode mode, EssayGradingTarget target, boolean generateFollowup) {
        String question = target.question();
        String answer = target.answer();
        Rubric rubric = target.rubric();
        boolean hasRubric = target.hasRubric();
        String rubricInstruction = hasRubric ? rubricInstruction(rubric) : NO_RUBRIC_INSTRUCTION;
        String followupInstruction = followupInstruction(generateFollowup, hasRubric ? rubric : null);

        return USER_PROMPT_TEMPLATE.formatted(
                question,
                answer,
                solvingTimeInstruction(target.solvingTime()),
                rubricInstruction,
                followupInstruction);
    }

    private String solvingTimeInstruction(SolvingTime solvingTime) {
        if (solvingTime == null || !solvingTime.isMeasured()) {
            return NO_SOLVING_TIME_INSTRUCTION;
        }
        return SOLVING_TIME_TEMPLATE.formatted(
                solvingTime.elapsedSeconds(), solvingTime.baselineSeconds(), paceText(solvingTime.pace()));
    }

    private String paceText(ElapsedPace pace) {
        return switch (pace) {
            case FAST -> "뚜렷하게 빠르다";
            case NORMAL -> "보통 수준이다";
            case SLOW -> "뚜렷하게 오래 걸렸다";
        };
    }

    private String rubricInstruction(Rubric rubric) {
        List<RubricCriterion> criteria = rubric.criteria();
        String numbered = IntStream.range(0, criteria.size())
                .mapToObj(index -> "%d. (배점 %d) %s".formatted(
                        index + 1, criteria.get(index).weight(), criteria.get(index).point()))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");

        return RUBRIC_INSTRUCTION_TEMPLATE.formatted(numbered);
    }

    // followupScope는 루브릭이 꼬리질문 가드레일로 함께 만들어 둔 값이다. 있으면 그대로 범위로 쓴다.
    private String followupInstruction(boolean generateFollowup, Rubric rubric) {
        if (!generateFollowup) {
            return NO_FOLLOWUP_INSTRUCTION;
        }
        if (rubric == null || !rubric.hasFollowupScope()) {
            return GENERATE_FOLLOWUP_INSTRUCTION;
        }

        StringBuilder scoped = new StringBuilder(GENERATE_FOLLOWUP_INSTRUCTION);
        List<String> allowed = rubric.followupScope().allowed();
        List<String> forbidden = rubric.followupScope().forbidden();
        if (!allowed.isEmpty()) {
            scoped.append("\n\n꼬리질문은 다음 개념 범위 안에서 물어라: ").append(String.join(", ", allowed));
        }
        if (!forbidden.isEmpty()) {
            scoped.append("\n다음 영역으로는 넘어가지 마라: ").append(String.join(", ", forbidden));
        }
        return scoped.toString();
    }
}
