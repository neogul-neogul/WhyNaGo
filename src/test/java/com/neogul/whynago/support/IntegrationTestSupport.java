package com.neogul.whynago.support;

import com.neogul.whynago.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class IntegrationTestSupport {

    @Autowired
    private DbCleaner dbCleaner;

    @BeforeEach
    void cleanDatabase() {
        dbCleaner.clear();
    }
}
