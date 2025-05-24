package com.example.demo.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all integration tests with built-in logging.
 * Extend this class instead of adding the annotations and logging to each test class.
 */
@SpringBootTest
@Transactional
@ExtendWith(TestLogger.class)
public abstract class BaseIntegrationTest {
    
    protected TestInfo testInfo;
    
    @BeforeEach
    void baseSetUp(TestInfo testInfo) {
        this.testInfo = testInfo;
        TestLogger.logTestStart(testInfo.getDisplayName());
    }
    
    @AfterEach
    void baseTearDown() {
        TestLogger.logTestEnd(testInfo.getDisplayName(), true);
    }
}
