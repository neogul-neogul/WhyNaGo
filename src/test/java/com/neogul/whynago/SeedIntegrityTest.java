package com.neogul.whynago;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.support.IntegrationTestSupport;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

// 배포되는 시드 파일을 실제 스키마에 그대로 넣어 검증한다.
// 태그 이름을 하나만 바꿔도 question_tag의 서브셀렉트가 NULL이 되어 조용히 태그가 사라지므로,
// 시드를 손볼 때마다 이 테스트가 먼저 깨지게 둔다.
class SeedIntegrityTest extends IntegrationTestSupport {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("태그 사전과 정규화된 시드가 실제 스키마에 그대로 들어간다.")
    void loadSeeds() throws Exception {
        // 로컬 프로파일이 로드하는 순서 그대로 넣는다. 컬럼이 추가됐는데 시드가 따라가지 못하면
        // 여기서 먼저 깨진다(테스트 유저 INSERT에 role이 빠져 부팅이 실패한 적이 있다).
        try (var connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("data-tag.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("data.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("data3.sql"));
        }

        assertThat(count("SELECT COUNT(*) FROM users")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM users WHERE email = 'test@test.test' AND role = 'USER'"))
                .isEqualTo(1);
        // 로컬에서 test 계정으로 로그인이 되는지까지 확인한다.
        assertThat(passwordEncoder.matches(
                "test",
                jdbc.queryForObject("SELECT password FROM users WHERE email = 'test@test.test'", String.class)))
                .isTrue();

        assertThat(count("SELECT COUNT(*) FROM tag")).isEqualTo(238);
        assertThat(count("SELECT COUNT(*) FROM tag WHERE category = 'DB'")).isEqualTo(31);
        assertThat(count("SELECT COUNT(*) FROM tag WHERE category = 'GENERAL_CS'")).isEqualTo(40);

        assertThat(count("SELECT COUNT(*) FROM question")).isEqualTo(600);
        assertThat(count("SELECT COUNT(*) FROM question WHERE type = 'MULTIPLE_CHOICE'")).isEqualTo(200);
        assertThat(count("SELECT COUNT(*) FROM question WHERE type = 'ESSAY'")).isEqualTo(400);
        // 새로 추가한 컬럼은 DB 기본값으로 채워진다.
        assertThat(count("SELECT COUNT(*) FROM question WHERE source = 'SEEDED'")).isEqualTo(600);
        assertThat(count("SELECT COUNT(*) FROM question WHERE review_status = 'APPROVED'")).isEqualTo(600);

        assertThat(count("SELECT COUNT(*) FROM question_tag")).isEqualTo(1505);
        assertThat(count("SELECT COUNT(*) FROM question_tag WHERE tag_id IS NULL")).isZero();
        assertThat(count("""
                SELECT COUNT(*) FROM question_tag qt
                WHERE NOT EXISTS (SELECT 1 FROM tag t WHERE t.id = qt.tag_id)
                """)).isZero();
        assertThat(count("""
                SELECT COUNT(*) FROM question_tag qt
                WHERE NOT EXISTS (SELECT 1 FROM question q WHERE q.id = qt.question_id)
                """)).isZero();
        assertThat(count("SELECT COUNT(*) FROM answer_choice WHERE related_question_id IS NULL")).isZero();
    }

    private int count(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}
