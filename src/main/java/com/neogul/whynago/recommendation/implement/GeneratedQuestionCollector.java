package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.recommendation.domain.GenerationTopic;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 취약 주제 목록을 저장된 생성 문항 목록으로 바꾼다.
// 검증 실패분은 재시도하지 않고 버리고, 쿼터 소진·AI 장애는 지금까지 만든 것만 남기고 멈춘다.
// 추천 자체가 실패하지 않는 것이 원칙이라 예외를 위로 올리지 않는다.
@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratedQuestionCollector {

    private final EssayQuestionGenerator essayQuestionGenerator;
    private final GeneratedQuestionAppender generatedQuestionAppender;

    public List<Question> collect(List<GenerationTopic> topics) {
        List<Question> generated = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (GenerationTopic topic : topics) {
            try {
                essayQuestionGenerator.generate(topic)
                        .map(generatedQuestionAppender::append)
                        .ifPresent(generated::add);
            } catch (BusinessException e) {
                log.warn("맞춤 문제 생성을 중단하고 폴백으로 넘어간다 - category={}, errorCode={}",
                        topic.category(), e.errorCode().code());
                break;
            }
        }
        log.info("맞춤 문제 생성 완료. time={}", System.currentTimeMillis() - start);
        return generated;
    }
}
