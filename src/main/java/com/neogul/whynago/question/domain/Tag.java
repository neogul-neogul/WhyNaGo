package com.neogul.whynago.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 태그 사전 그 자체다(docs/TAG.md가 원본). 문항이 아직 없는 태그도 행으로 존재하므로,
// 생성 후보 태그를 "그 카테고리에 문항이 있는 태그"가 아니라 사전 전체에서 고를 수 있다.
// 사전 확장은 별도 작업이라 런타임에 태그를 새로 만드는 경로는 두지 않는다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리를 통틀어 유일하다. 표기 변형을 새 태그로 만들지 않기 위해 유일 제약을 건다.
    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    private Tag(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public static Tag of(String name, Category category) {
        return new Tag(name, category);
    }
}
