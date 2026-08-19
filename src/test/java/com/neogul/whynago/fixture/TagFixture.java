package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Tag;

public final class TagFixture {

    private TagFixture() {
    }

    public static Tag of(String name, Category category) {
        return Tag.of(name, category);
    }

    // 카테고리가 검증 의도에 중요하지 않은 테스트가 대부분이라 기본 카테고리 헬퍼를 둔다.
    public static Tag db(String name) {
        return Tag.of(name, Category.DB);
    }
}
