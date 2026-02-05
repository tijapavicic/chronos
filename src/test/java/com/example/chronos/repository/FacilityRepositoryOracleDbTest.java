package com.example.chronos.repository;

import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight integration tests for FacilityRepository against a manually provisioned Oracle DB.
 *
 * Unlike FacilityRepositoryIntegrationTest, this class does NOT use Testcontainers.
 * It expects an Oracle database to be running and accessible via environment variables:
 *
 * Required environment variables:
 * - ORACLE_TEST_URL: Oracle JDBC URL (e.g., jdbc:oracle:thin:@localhost:1521:xe)
 * - ORACLE_TEST_USERNAME: Database username
 * - ORACLE_TEST_PASSWORD: Database password
 *
 * Tests are automatically skipped if Oracle DB is not reachable.
 *
 * Example Docker command to start Oracle XE:
 * docker run -d -p 1521:1521 -e ORACLE_PASSWORD=oracle gvenzl/oracle-xe:21-slim-faststart
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=${ORACLE_TEST_URL:jdbc:oracle:thin:@localhost:1521:xe}",
        "spring.datasource.username=${ORACLE_TEST_USERNAME:system}",
        "spring.datasource.password=${ORACLE_TEST_PASSWORD:oracle}",
        "spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
        "spring.jpa.database-platform=org.hibernate.dialect.OracleDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@EnabledIfOracleAvailable
@DisplayName("Facility Repository Oracle DB Tests (External DB)")
class FacilityRepositoryOracleDbTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @BeforeEach
    void setUp() {
        facilityRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        facilityRepository.deleteAll();
    }

    @Test
    @DisplayName("Should verify Oracle connection and basic CRUD operations")
    void shouldVerifyOracleConnectionAndCrud() {
        // Given
        Facility facility = Facility.builder()
                .facilityName("Oracle Test Facility")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(true)
                .build();

        // When - Create
        Facility saved = facilityRepository.save(facility);
        assertThat(saved.getFacilityId()).isNotNull();

        // When - Read
        Facility found = facilityRepository.findById(saved.getFacilityId()).orElseThrow();
        assertThat(found.getFacilityName()).isEqualTo("Oracle Test Facility");

        // When - Update
        found.setFacilityName("Updated Oracle Facility");
        facilityRepository.save(found);

        // When - Read Updated
        Facility updated = facilityRepository.findById(saved.getFacilityId()).orElseThrow();
        assertThat(updated.getFacilityName()).isEqualTo("Updated Oracle Facility");

        // When - Delete
        facilityRepository.deleteById(saved.getFacilityId());

        // Then - Verify Deletion
        assertThat(facilityRepository.findById(saved.getFacilityId())).isEmpty();
    }

    @Test
    @DisplayName("Should execute custom query methods on Oracle")
    void shouldExecuteCustomQueryMethods() {
        // Given
        Facility prod1 = Facility.builder()
                .facilityName("Oracle Production 1")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(true)
                .build();

        Facility prod2 = Facility.builder()
                .facilityName("Oracle Production 2")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(false)
                .build();

        Facility warehouse = Facility.builder()
                .facilityName("Oracle Warehouse")
                .facilityType(FacilityType.WAREHOUSE)
                .extendable(true)
                .build();

        facilityRepository.saveAll(List.of(prod1, prod2, warehouse));

        // When
        List<Facility> productionFacilities = facilityRepository.findByFacilityType(FacilityType.PRODUCTION);
        List<Facility> warehouseFacilities = facilityRepository.findByFacilityType(FacilityType.WAREHOUSE);
        List<Facility> byName = facilityRepository.findByFacilityName("Oracle Production 1");

        // Then
        assertThat(productionFacilities).hasSize(2);
        assertThat(warehouseFacilities).hasSize(1);
        assertThat(byName).hasSize(1);
        assertThat(byName.get(0).getFacilityName()).isEqualTo("Oracle Production 1");
    }

    @Test
    @DisplayName("Should handle Oracle-specific UUID generation")
    void shouldHandleOracleUuidGeneration() {
        // Given
        Facility facility = Facility.builder()
                .facilityName("UUID Test")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(false)
                .build();

        // When
        Facility saved = facilityRepository.save(facility);

        // Then
        assertThat(saved.getFacilityId()).isNotNull();
        assertThat(saved.getFacilityId().toString()).matches(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
        );
    }

    @Test
    @DisplayName("Should handle batch operations efficiently on Oracle")
    void shouldHandleBatchOperationsEfficiently() {
        // Given
        List<Facility> facilities = List.of(
                Facility.builder().facilityName("Batch 1").facilityType(FacilityType.PRODUCTION).extendable(true).build(),
                Facility.builder().facilityName("Batch 2").facilityType(FacilityType.WAREHOUSE).extendable(false).build(),
                Facility.builder().facilityName("Batch 3").facilityType(FacilityType.PRODUCTION).extendable(true).build(),
                Facility.builder().facilityName("Batch 4").facilityType(FacilityType.WAREHOUSE).extendable(false).build(),
                Facility.builder().facilityName("Batch 5").facilityType(FacilityType.PRODUCTION).extendable(true).build()
        );

        // When
        List<Facility> savedFacilities = facilityRepository.saveAll(facilities);

        // Then
        assertThat(savedFacilities).hasSize(5);
        assertThat(facilityRepository.count()).isEqualTo(5);
        assertThat(savedFacilities).allMatch(f -> f.getFacilityId() != null);
    }
}
