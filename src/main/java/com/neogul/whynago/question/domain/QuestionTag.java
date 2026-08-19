package com.neogul.whynago.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 문항과 태그 사전을 잇는 조인 엔티티다. 태그 이름을 여기 두면 같은 문자열이 행마다 반복되고,
// 사용자별 숙련도를 태그 단위로 붙일 대상(tag_id)도 생기지 않는다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Long tagId;

    private QuestionTag(Long questionId, Long tagId) {
        this.questionId = questionId;
        this.tagId = tagId;
    }

    public static QuestionTag create(Long questionId, Long tagId) {
        return new QuestionTag(questionId, tagId);
    }
}
