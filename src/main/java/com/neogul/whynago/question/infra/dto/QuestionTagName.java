package com.neogul.whynago.question.infra.dto;

// 문항에 붙은 태그 이름 한 건. 정규화 이후 이름은 tag 테이블에 있으므로 조인 결과로 받는다.
public interface QuestionTagName {

    Long getQuestionId();

    String getName();
}
