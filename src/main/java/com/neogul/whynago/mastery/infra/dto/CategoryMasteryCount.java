package com.neogul.whynago.mastery.infra.dto;

import com.neogul.whynago.question.domain.Category;

// 카테고리 x 숙련도별 판정 횟수. "어떤 숙련도를 몇 번 받았는지"를 보여주는 데 쓴다.
public interface CategoryMasteryCount {

    Category getCategory();

    String getLevel();

    long getCount();
}
