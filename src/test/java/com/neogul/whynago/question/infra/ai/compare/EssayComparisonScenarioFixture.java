package com.neogul.whynago.question.infra.ai.compare;

import com.neogul.whynago.question.domain.EssayGradingMode;
import java.util.List;

/**
 * 모델 차이가 드러나기 쉬운 지점(정답·부분오답·모른다는 답변·멀티턴·채점 전용)으로 시나리오를 구성한다.
 */
public class EssayComparisonScenarioFixture {

    public static List<EssayComparisonScenario> scenarios() {
        return List.of(
                correctAnswer(),
                partiallyWrongAnswer(),
                dontKnowAnswer(),
                interviewFollowupChain(),
                gradeOnly());
    }

    public static EssayComparisonScenario correctAnswer() {
        return new EssayComparisonScenario(
                "정확한 답변",
                EssayGradingMode.PRACTICE,
                "HTTP와 HTTPS의 차이를 설명해 주세요.",
                List.of("""
                        HTTPS는 HTTP 메시지를 TLS로 암호화해서 주고받습니다.
                        서버는 인증서로 자신을 증명하고, 핸드셰이크 과정에서 대칭키를 교환한 뒤
                        그 키로 본문을 암호화하기 때문에 중간에서 내용을 보거나 변조하기 어렵습니다.
                        기본 포트는 HTTP가 80, HTTPS가 443입니다."""),
                true);
    }

    public static EssayComparisonScenario partiallyWrongAnswer() {
        return new EssayComparisonScenario(
                "부분적으로 틀린 답변",
                EssayGradingMode.PRACTICE,
                "데이터베이스 인덱스가 조회 성능을 높이는 이유를 설명해 주세요.",
                List.of("""
                        인덱스를 걸면 그 테이블 데이터가 전부 메모리에 올라가서 디스크를 안 읽어도 되니까 빨라집니다.
                        그래서 컬럼마다 인덱스를 많이 걸수록 조회는 물론 삽입도 같이 빨라집니다."""),
                true);
    }

    // 반복 호출 비교용. 같은 문답이지만 모델 간 비교 결과와 리포트에서 섞이지 않도록 이름을 따로 둔다.
    public static EssayComparisonScenario repeatedCalls() {
        EssayComparisonScenario base = partiallyWrongAnswer();
        return new EssayComparisonScenario(
                base.name() + " (반복 호출)",
                base.mode(),
                base.question(),
                base.answers(),
                base.requestFollowupOnLastTurn());
    }

    public static EssayComparisonScenario dontKnowAnswer() {
        return new EssayComparisonScenario(
                "모른다고 답한 답변",
                EssayGradingMode.PRACTICE,
                "트랜잭션 격리 수준 중 REPEATABLE READ가 무엇을 보장하는지 설명해 주세요.",
                List.of("잘 모르겠습니다."),
                true);
    }

    public static EssayComparisonScenario interviewFollowupChain() {
        return new EssayComparisonScenario(
                "면접 모드 멀티턴",
                EssayGradingMode.INTERVIEW,
                "REST API를 설계할 때 무엇을 고려하시나요?",
                List.of(
                        """
                        자원을 명사로 표현하고 행위는 HTTP 메서드로 나타냅니다.
                        조회는 GET, 생성은 POST를 쓰고 응답 상태 코드도 의미에 맞게 줍니다.""",
                        "음... 상태 코드는 성공이면 200, 실패면 400이나 500을 준다는 정도로 알고 있습니다.",
                        "거기까지는 잘 모르겠습니다."),
                true);
    }

    public static EssayComparisonScenario gradeOnly() {
        return new EssayComparisonScenario(
                "꼬리질문 없이 채점만",
                EssayGradingMode.PRACTICE,
                "프로세스와 스레드의 차이를 설명해 주세요.",
                List.of("""
                        프로세스는 실행 중인 프로그램 단위로 각자 메모리 공간을 가지고,
                        스레드는 그 프로세스 안에서 코드와 힙을 공유하며 스택만 따로 가집니다."""),
                false);
    }
}
