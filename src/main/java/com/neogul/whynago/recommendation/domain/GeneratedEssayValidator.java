package com.neogul.whynago.recommendation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

// AI 출력은 신뢰하지 않는다. 형식·요청 일치·중복을 전수 검사하고 하나라도 어기면 그 문항을 버린다.
// 내용의 사실 정확성은 여기서 검증할 수 없다. 그 부분은 검수 승인(PENDING -> APPROVED)이 맡는다.
public class GeneratedEssayValidator {

    private static final int MIN_CONTENT_LENGTH = 20;
    private static final int MIN_MODEL_ANSWER_LENGTH = 30;
    private static final int MIN_GRADING_CRITERIA = 2;
    private static final int MIN_TAGS = 1;
    private static final int MAX_TAGS = 2;

    // 위반 사유 목록을 돌려준다. 비어 있으면 통과다. 어떤 규칙에서 버려졌는지 로그로 남기기 위해
    // boolean이 아니라 사유를 반환한다.
    public List<String> validate(
            GeneratedEssay candidate,
            GenerationTopic topic,
            Set<String> knownTags,
            Set<String> existingTexts
    ) {
        List<String> violations = new ArrayList<>();

        if (isBlank(candidate.title())) {
            violations.add("제목이 비어 있음");
        }
        if (isBlank(candidate.content()) || candidate.content().strip().length() < MIN_CONTENT_LENGTH) {
            violations.add("발문이 비었거나 %d자 미만".formatted(MIN_CONTENT_LENGTH));
        }
        if (isBlank(candidate.modelAnswer())
                || candidate.modelAnswer().strip().length() < MIN_MODEL_ANSWER_LENGTH) {
            violations.add("모범답안이 비었거나 %d자 미만".formatted(MIN_MODEL_ANSWER_LENGTH));
        }
        if (candidate.gradingCriteria() == null || candidate.gradingCriteria().size() < MIN_GRADING_CRITERIA) {
            violations.add("채점 기준이 %d개 미만".formatted(MIN_GRADING_CRITERIA));
        }
        if (candidate.category() != topic.category()) {
            violations.add("요청한 카테고리(%s)와 다름".formatted(topic.category()));
        }
        if (candidate.difficulty() != topic.targetDifficulty()) {
            violations.add("요청한 목표 난이도(%s)와 다름".formatted(topic.targetDifficulty()));
        }
        violations.addAll(validateTags(candidate, topic, knownTags));
        violations.addAll(validateDuplication(candidate, existingTexts));

        return violations;
    }

    private List<String> validateTags(GeneratedEssay candidate, GenerationTopic topic, Set<String> knownTags) {
        List<String> tags = candidate.tags() == null ? List.of() : candidate.tags();
        if (tags.size() < MIN_TAGS || tags.size() > MAX_TAGS) {
            return List.of("태그 수가 %d~%d개 범위를 벗어남".formatted(MIN_TAGS, MAX_TAGS));
        }

        List<String> violations = new ArrayList<>();
        // 사전에 없는 태그는 생성 중에 만들지 않는다. 만들면 태그 사전이 조용히 오염된다.
        List<String> unknown = tags.stream().filter(tag -> !knownTags.contains(tag)).toList();
        if (!unknown.isEmpty()) {
            violations.add("사전에 없는 태그: %s".formatted(String.join(", ", unknown)));
        }
        // 요청한 태그가 있었다면 그중 최소 1개는 반드시 포함해야 한다. 그렇지 않으면 취약 주제를 벗어난다.
        if (!topic.tags().isEmpty() && topic.tags().stream().noneMatch(tags::contains)) {
            violations.add("요청한 태그(%s)를 하나도 포함하지 않음".formatted(String.join(", ", topic.tags())));
        }
        return violations;
    }

    // 비교 기준을 한쪽만 다듬으면 대소문자·공백 차이로 중복을 놓친다. 양쪽 모두 같은 방식으로 정규화한다.
    private List<String> validateDuplication(GeneratedEssay candidate, Set<String> existingTexts) {
        Set<String> normalized = existingTexts.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        List<String> violations = new ArrayList<>();
        if (normalized.contains(normalize(candidate.title()))) {
            violations.add("기존 문항 제목과 완전히 같음");
        }
        if (normalized.contains(normalize(candidate.content()))) {
            violations.add("기존 문항 발문과 완전히 같음");
        }
        return violations;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.strip().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
