package com.neogul.whynago.support;

import com.neogul.whynago.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DataJpaTest
public abstract class RepositoryTestSupport {
}
