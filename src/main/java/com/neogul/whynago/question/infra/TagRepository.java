package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByCategoryOrderById(Category category);

    List<Tag> findByNameIn(List<String> names);
}
