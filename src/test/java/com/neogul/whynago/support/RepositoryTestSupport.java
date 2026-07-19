package com.neogul.whynago.support;

import com.neogul.whynago.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

//@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DataJpaTest
public abstract class RepositoryTestSupport {

    @Autowired
    protected TestEntityManager em;
}
