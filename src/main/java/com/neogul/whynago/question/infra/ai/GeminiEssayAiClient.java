package com.neogul.whynago.question.infra.ai;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class GeminiEssayAiClient implements EssayAiClient {

    private final ChatClient chatClient;

    public GeminiEssayAiClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public GradedAnswer grade(List<EssayTurn> thread) {
        EssayTurn target = thread.get(thread.size() - 1);
        String prompt = """
                너는 개발자 채용 기술 면접관이다. 아래 면접 문답에서 '채점 대상' 답변을 평가하라.
                한국어로 작성하고, 정답을 단정하기보다 보완할 점 중심으로 피드백하라.

                [지금까지의 문답]
                %s

                [채점 대상]
                질문: %s
                답변: %s
                """.formatted(renderThread(thread), target.question(), target.answer());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(GradedAnswer.class);
    }

    @Override
    public GeneratedFollowup generateFollowup(List<EssayTurn> thread) {
        String prompt = """
                너는 개발자 채용 기술 면접관이다. 아래 문답 흐름에 이어서 지원자의 이해도를 더 깊이 확인할
                꼬리질문 한 개를 한국어로 생성하라. 새로운 주제로 벗어나지 말고 직전 답변을 파고들어라.

                [지금까지의 문답]
                %s
                """.formatted(renderThread(thread));

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(GeneratedFollowup.class);
    }

    private String renderThread(List<EssayTurn> thread) {
        return IntStream.range(0, thread.size())
                .mapToObj(index -> {
                    EssayTurn turn = thread.get(index);
                    return "%d. 질문: %s%n   답변: %s".formatted(index + 1, turn.question(), turn.answer());
                })
                .reduce((a, b) -> a + System.lineSeparator() + b)
                .orElse("");
    }
}
