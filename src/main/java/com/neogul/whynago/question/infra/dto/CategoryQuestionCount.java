package com.neogul.whynago.question.infra.dto;

import com.neogul.whynago.question.domain.Category;

public interface CategoryQuestionCount {

    Category getCategory();

    long getTotal();
}
