package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Test logger utility to provide detailed logging for test execution.
 * Can be used as a JUnit 5 extension or directly in test methods.
 */
public class TestLogger implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    
    private static final Logger log = LoggerFactory.getLogger(TestLogger.class);
    
    /**
     * Log the start of a test method
     * @param testName Name of the test method
     */
    public static void logTestStart(String testName) {
        String separator = "=".repeat(80);
        log.info("\n{}\n ▶️ STARTING TEST: {}\n{}", separator, testName, separator);
    }
    
    /**
     * Log the end of a test method
     * @param testName Name of the test method
     * @param success Whether the test was successful
     */
    public static void logTestEnd(String testName, boolean success) {
        String separator = "=".repeat(80);
        String status = success ? "✅ PASSED" : "❌ FAILED";
        log.info("\n{}\n {} TEST: {}\n{}", separator, status, testName, separator);
    }
    
    /**
     * Log a test step
     * @param stepDescription Description of the test step
     */
    public static void logStep(String stepDescription) {
        log.info("   📌 STEP: {}", stepDescription);
    }
    
    /**
     * Log an assertion
     * @param assertionDescription Description of the assertion
     */
    public static void logAssertion(String assertionDescription) {
        log.info("      🔍 ASSERT: {}", assertionDescription);
    }
    
    /**
     * Log test data
     * @param dataDescription Description of the data
     * @param data The actual data (will be converted to string)
     */
    public static void logData(String dataDescription, Object data) {
        log.info("         📊 DATA: {} = {}", dataDescription, data);
    }
    
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        logTestStart(context.getDisplayName());
    }
    
    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean success = context.getExecutionException().isEmpty();
        logTestEnd(context.getDisplayName(), success);
    }
}
