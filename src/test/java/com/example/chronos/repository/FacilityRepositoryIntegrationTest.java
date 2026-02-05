package com.example.chronos.repository;

import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FacilityRepository that execute conditionally when Oracle DB is available.
 *
 * These tests use Testcontainers to spin up a real Oracle XE database container.
 * Tests are automatically skipped if Docker is not available or Oracle container cannot start.
 *
 * To run these tests:
 * 1. Ensure Docker is running
 * 2. Run: mvn test -Dtest=FacilityRepositoryIntegrationTest
 *
 * Environment variables can override defaults:
 * - ORACLE_TEST_URL: Oracle JDBC URL (default: jdbc:oracle:thin:@localhost:1521:xe)
 * - ORACLE_TEST_USERNAME: Database username (default: system)
 * - ORACLE_TEST_PASSWORD: Database password (default: oracle)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OracleTestContainerConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true"
})
@EnabledIfOracleAvailable
@DisplayName("Facility Repository Oracle Integration Tests")
class FacilityRepositoryIntegrationTest {

    @Autowired
    private FacilityRepository facilityRepository;

    private Facility testFacility1;
    private Facility testFacility2;
    private Facility testFacility3;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        facilityRepository.deleteAll();

        // Create test fixtures
        testFacility1 = Facility.builder()
                .facilityName("Production Line A")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(true)
                .build();

        testFacility2 = Facility.builder()
                .facilityName("Warehouse 1")
                .facilityType(FacilityType.WAREHOUSE)
                .extendable(false)
                .build();

        testFacility3 = Facility.builder()
                .facilityName("Production Line B")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        facilityRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve a facility with all fields")
    void shouldSaveAndRetrieveFacility() {
        // Given
        Facility savedFacility = facilityRepository.save(testFacility1);

        // When
        Optional<Facility> retrievedFacility = facilityRepository.findById(savedFacility.getFacilityId());

        // Then
        assertThat(retrievedFacility).isPresent();
        assertThat(retrievedFacility.get().getFacilityName()).isEqualTo("Production Line A");
        assertThat(retrievedFacility.get().getFacilityType()).isEqualTo(FacilityType.PRODUCTION);
        assertThat(retrievedFacility.get().isExtendable()).isTrue();
        assertThat(retrievedFacility.get().getFacilityId()).isNotNull();
    }

    @Test
    @DisplayName("Should generate UUID automatically on save")
    void shouldGenerateUuidAutomatically() {
        // Given - facility without ID
        assertThat(testFacility1.getFacilityId()).isNull();

        // When
        Facility savedFacility = facilityRepository.save(testFacility1);

        // Then
        assertThat(savedFacility.getFacilityId()).isNotNull();
        assertThat(savedFacility.getFacilityId()).isInstanceOf(UUID.class);
    }

    @Test
    @DisplayName("Should find facilities by facility name")
    void shouldFindByFacilityName() {
        // Given
        facilityRepository.save(testFacility1);
        facilityRepository.save(testFacility2);
        facilityRepository.save(testFacility3);

        // When
        List<Facility> productionLineA = facilityRepository.findByFacilityName("Production Line A");
        List<Facility> warehouse1 = facilityRepository.findByFacilityName("Warehouse 1");
        List<Facility> nonExistent = facilityRepository.findByFacilityName("Non Existent");

        // Then
        assertThat(productionLineA).hasSize(1);
        assertThat(productionLineA.get(0).getFacilityName()).isEqualTo("Production Line A");

        assertThat(warehouse1).hasSize(1);
        assertThat(warehouse1.get(0).getFacilityType()).isEqualTo(FacilityType.WAREHOUSE);

        assertThat(nonExistent).isEmpty();
    }

    @Test
    @DisplayName("Should find all facilities by facility type")
    void shouldFindByFacilityType() {
        // Given
        facilityRepository.save(testFacility1);
        facilityRepository.save(testFacility2);
        facilityRepository.save(testFacility3);

        // When
        List<Facility> productionFacilities = facilityRepository.findByFacilityType(FacilityType.PRODUCTION);
        List<Facility> warehouseFacilities = facilityRepository.findByFacilityType(FacilityType.WAREHOUSE);

        // Then
        assertThat(productionFacilities).hasSize(2);
        assertThat(productionFacilities)
                .extracting(Facility::getFacilityName)
                .containsExactlyInAnyOrder("Production Line A", "Production Line B");

        assertThat(warehouseFacilities).hasSize(1);
        assertThat(warehouseFacilities.get(0).getFacilityName()).isEqualTo("Warehouse 1");
    }

    @Test
    @DisplayName("Should return empty list when no facilities match type")
    void shouldReturnEmptyListWhenNoMatch() {
        // Given
        facilityRepository.save(testFacility1); // PRODUCTION type only

        // When
        List<Facility> warehouseFacilities = facilityRepository.findByFacilityType(FacilityType.WAREHOUSE);

        // Then
        assertThat(warehouseFacilities).isEmpty();
    }

    @Test
    @DisplayName("Should update existing facility")
    void shouldUpdateExistingFacility() {
        // Given
        Facility savedFacility = facilityRepository.save(testFacility1);
        UUID facilityId = savedFacility.getFacilityId();

        // When
        savedFacility.setFacilityName("Updated Production Line");
        savedFacility.setExtendable(false);
        facilityRepository.save(savedFacility);

        // Then
        Optional<Facility> updatedFacility = facilityRepository.findById(facilityId);
        assertThat(updatedFacility).isPresent();
        assertThat(updatedFacility.get().getFacilityName()).isEqualTo("Updated Production Line");
        assertThat(updatedFacility.get().isExtendable()).isFalse();
        assertThat(updatedFacility.get().getFacilityId()).isEqualTo(facilityId);
    }

    @Test
    @DisplayName("Should delete facility by ID")
    void shouldDeleteFacilityById() {
        // Given
        Facility savedFacility = facilityRepository.save(testFacility1);
        UUID facilityId = savedFacility.getFacilityId();
        assertThat(facilityRepository.findById(facilityId)).isPresent();

        // When
        facilityRepository.deleteById(facilityId);

        // Then
        assertThat(facilityRepository.findById(facilityId)).isEmpty();
    }

    @Test
    @DisplayName("Should delete facility by entity")
    void shouldDeleteFacilityByEntity() {
        // Given
        Facility savedFacility = facilityRepository.save(testFacility1);
        UUID facilityId = savedFacility.getFacilityId();

        // When
        facilityRepository.delete(savedFacility);

        // Then
        assertThat(facilityRepository.findById(facilityId)).isEmpty();
    }

    @Test
    @DisplayName("Should find all facilities")
    void shouldFindAllFacilities() {
        // Given
        facilityRepository.save(testFacility1);
        facilityRepository.save(testFacility2);
        facilityRepository.save(testFacility3);

        // When
        List<Facility> allFacilities = facilityRepository.findAll();

        // Then
        assertThat(allFacilities).hasSize(3);
        assertThat(allFacilities)
                .extracting(Facility::getFacilityName)
                .containsExactlyInAnyOrder("Production Line A", "Warehouse 1", "Production Line B");
    }

    @Test
    @DisplayName("Should count facilities correctly")
    void shouldCountFacilities() {
        // Given - initially empty
        assertThat(facilityRepository.count()).isZero();

        // When
        facilityRepository.save(testFacility1);
        facilityRepository.save(testFacility2);

        // Then
        assertThat(facilityRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should check if facility exists by ID")
    void shouldCheckIfFacilityExists() {
        // Given
        Facility savedFacility = facilityRepository.save(testFacility1);
        UUID existingId = savedFacility.getFacilityId();
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThat(facilityRepository.existsById(existingId)).isTrue();
        assertThat(facilityRepository.existsById(nonExistentId)).isFalse();
    }

    @Test
    @DisplayName("Should handle multiple facilities with same type")
    void shouldHandleMultipleFacilitiesWithSameType() {
        // Given - create 5 production facilities
        for (int i = 1; i <= 5; i++) {
            Facility facility = Facility.builder()
                    .facilityName("Production Line " + i)
                    .facilityType(FacilityType.PRODUCTION)
                    .extendable(i % 2 == 0)
                    .build();
            facilityRepository.save(facility);
        }

        // When
        List<Facility> productionFacilities = facilityRepository.findByFacilityType(FacilityType.PRODUCTION);

        // Then
        assertThat(productionFacilities).hasSize(5);
        assertThat(productionFacilities)
                .allMatch(f -> f.getFacilityType() == FacilityType.PRODUCTION);
    }

    @Test
    @DisplayName("Should persist boolean field correctly")
    void shouldPersistBooleanFieldCorrectly() {
        // Given
        Facility extendableFacility = Facility.builder()
                .facilityName("Extendable Facility")
                .facilityType(FacilityType.PRODUCTION)
                .extendable(true)
                .build();

        Facility nonExtendableFacility = Facility.builder()
                .facilityName("Non-Extendable Facility")
                .facilityType(FacilityType.WAREHOUSE)
                .extendable(false)
                .build();

        // When
        Facility savedExtendable = facilityRepository.save(extendableFacility);
        Facility savedNonExtendable = facilityRepository.save(nonExtendableFacility);

        // Then
        assertThat(facilityRepository.findById(savedExtendable.getFacilityId()))
                .isPresent()
                .get()
                .extracting(Facility::isExtendable)
                .isEqualTo(true);

        assertThat(facilityRepository.findById(savedNonExtendable.getFacilityId()))
                .isPresent()
                .get()
                .extracting(Facility::isExtendable)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("Should handle transaction rollback on error")
    void shouldHandleTransactionRollback() {
        // Given
        long initialCount = facilityRepository.count();

        // When - try to save null (should fail)
        try {
            facilityRepository.save(null);
            fail("Expected exception when saving null");
        } catch (Exception e) {
            // Expected
        }

        // Then - count should remain the same
        assertThat(facilityRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Should maintain data integrity across transactions")
    void shouldMaintainDataIntegrityAcrossTransactions() {
        // Given & When
        Facility facility1 = facilityRepository.save(testFacility1);
        facilityRepository.flush(); // Force synchronization with DB

        Facility facility2 = facilityRepository.save(testFacility2);
        facilityRepository.flush();

        // Then
        List<Facility> allFacilities = facilityRepository.findAll();
        assertThat(allFacilities).hasSize(2);
        assertThat(allFacilities)
                .extracting(Facility::getFacilityId)
                .containsExactlyInAnyOrder(facility1.getFacilityId(), facility2.getFacilityId());
    }
}
