package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.infra.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagReader {

    private final TagRepository tagRepository;

    public List<String> readNamesByCategory(Category category) {
        return tagRepository.findByCategoryOrderById(category).stream()
                .map(Tag::getName)
                .toList();
    }

    public Map<Long, Tag> readByIds(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return Map.of();
        }
        return tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));
    }

    // 사전에 없는 이름은 맵에서 빠진다. 존재 검증은 호출자가 한다.
    public Map<String, Long> readIdsByNames(List<String> names) {
        if (names.isEmpty()) {
            return Map.of();
        }
        return tagRepository.findByNameIn(names).stream()
                .collect(Collectors.toMap(Tag::getName, Tag::getId));
    }
}
