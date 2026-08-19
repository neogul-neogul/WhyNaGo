package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionAppender {

    private final QuestionRepository questionRepository;
    private final QuestionTagRepository questionTagRepository;
    private final TagReader tagReader;

    // 문항과 태그는 항상 함께 저장한다. 태그 없는 문항은 약점 프로필에서 카테고리로만 잡힌다.
    public Question append(Question question, List<String> tagNames) {
        List<Long> tagIds = resolveTagIds(tagNames);
        Question saved = questionRepository.save(question);
        questionTagRepository.saveAll(tagIds.stream()
                .map(tagId -> QuestionTag.create(saved.getId(), tagId))
                .toList());
        return saved;
    }

    // 사전에 없는 태그는 만들지 않는다. 생성 문항은 검증기가 먼저 걸러내므로 여기까지 오면 버그다.
    private List<Long> resolveTagIds(List<String> tagNames) {
        Map<String, Long> idsByName = tagReader.readIdsByNames(tagNames);
        return tagNames.stream()
                .map(name -> {
                    Long tagId = idsByName.get(name);
                    if (tagId == null) {
                        throw new BusinessException(QuestionErrorCode.TAG_NOT_FOUND);
                    }
                    return tagId;
                })
                .toList();
    }
}
