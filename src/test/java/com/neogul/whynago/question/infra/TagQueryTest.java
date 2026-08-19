package com.neogul.whynago.question.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

// 태그 정규화 이후 "태그 이름으로 좁히는" 쿼리들이 조인으로도 같은 결과를 주는지 확인한다.
class TagQueryTest extends RepositoryTestSupport {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("태그 사전은 카테고리로 조회하며 문항이 없는 태그도 포함한다.")
    void findByCategory() {
        tagRepository.save(TagFixture.of("인덱스", Category.DB));
        tagRepository.save(TagFixture.of("TCP", Category.NETWORK));

        // 어떤 문항에도 붙지 않은 태그다. 정규화 이전에는 문항이 없으면 사전에서 사라졌다.
        assertThat(tagRepository.findByCategoryOrderById(Category.DB))
                .extracting(Tag::getName)
                .containsExactly("인덱스");
    }

    @Test
    @DisplayName("문항의 태그 이름은 부여 순서대로 조회된다.")
    void findTagNames() {
        Question question = questionRepository.save(QuestionFixture.essayRoot());
        Tag primary = tagRepository.save(TagFixture.db("인덱스"));
        Tag related = tagRepository.save(TagFixture.db("실행 계획"));
        questionTagRepository.save(QuestionTag.create(question.getId(), primary.getId()));
        questionTagRepository.save(QuestionTag.create(question.getId(), related.getId()));

        assertThat(questionTagRepository.findTagNames(List.of(question.getId())))
                .extracting(name -> name.getName())
                // 첫 행이 주 태그라는 시드 규칙이 조회 순서에도 남아야 한다.
                .containsExactly("인덱스", "실행 계획");
    }

    @Test
    @DisplayName("태그 이름으로 그 태그가 붙은 문항의 오답 해설만 조회한다.")
    void findWrongExplanationsByTagNames() {
        Question tagged = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question untagged = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Tag tag = tagRepository.save(TagFixture.db("인덱스"));
        questionTagRepository.save(QuestionTag.create(tagged.getId(), tag.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(tagged.getId(), 1));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(untagged.getId(), 1));

        List<String> explanations =
                answerChoiceRepository.findWrongExplanationsByTagNames(List.of("인덱스"), PageRequest.of(0, 5));

        assertThat(explanations).hasSize(1);
    }

    @Test
    @DisplayName("서술형 제목은 카테고리 또는 태그 이름으로 조회한다.")
    void findEssayTitles() {
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        Tag tag = tagRepository.save(TagFixture.of("TCP", Category.NETWORK));
        questionTagRepository.save(QuestionTag.create(essay.getId(), tag.getId()));

        // 카테고리가 달라도 태그로 걸린다.
        assertThat(questionRepository.findEssayTitles(Category.NETWORK, List.of("TCP")))
                .containsExactly(essay.getTitle());
    }
}
