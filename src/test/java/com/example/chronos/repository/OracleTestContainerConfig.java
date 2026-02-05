package com.example.chronos.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration for Oracle Database using Testcontainers.
 * This will automatically start an Oracle XE container when tests are run.
 */
@TestConfiguration(proxyBeanMethods = false)
public class OracleTestContainerConfig {

    /**
     * Creates and configures an Oracle XE container for testing.
     * The @ServiceConnection annotation automatically configures Spring Boot datasource properties.
     */
    @Bean
    @ServiceConnection
    OracleContainer oracleContainer() {
        OracleContainer container = new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true); // Reuse container across test runs for faster execution

        container.start();
        return container;
    }
}
