package com.example.chronos.repository;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * JUnit 5 execution condition that checks if Oracle database is available.
 * Tests will be skipped if connection cannot be established.
 */
public class OracleAvailableCondition implements ExecutionCondition {

    private static final Logger log = LoggerFactory.getLogger(OracleAvailableCondition.class);

    // Oracle DB connection properties - can be overridden via system properties or environment variables
    private static final String ORACLE_URL = System.getProperty("oracle.test.url",
            System.getenv().getOrDefault("ORACLE_TEST_URL", "jdbc:oracle:thin:@localhost:1521:xe"));
    private static final String ORACLE_USER = System.getProperty("oracle.test.username",
            System.getenv().getOrDefault("ORACLE_TEST_USERNAME", "system"));
    private static final String ORACLE_PASSWORD = System.getProperty("oracle.test.password",
            System.getenv().getOrDefault("ORACLE_TEST_PASSWORD", "oracle"));

    private static Boolean oracleAvailable = null;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (oracleAvailable == null) {
            oracleAvailable = checkOracleAvailability();
        }

        if (oracleAvailable) {
            return ConditionEvaluationResult.enabled("Oracle database is available at " + ORACLE_URL);
        } else {
            return ConditionEvaluationResult.disabled("Oracle database is not available at " + ORACLE_URL);
        }
    }

    private boolean checkOracleAvailability() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            try (Connection conn = DriverManager.getConnection(ORACLE_URL, ORACLE_USER, ORACLE_PASSWORD)) {
                log.info("Successfully connected to Oracle DB at {}", ORACLE_URL);
                return conn.isValid(2);
            }
        } catch (Exception e) {
            log.warn("Oracle database not available: {}", e.getMessage());
            return false;
        }
    }
}
