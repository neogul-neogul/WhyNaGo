package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.QuestionTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

    List<QuestionTag> findByQuestionIdIn(List<Long> questionIds);
}
