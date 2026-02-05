package com.example.chronos.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Facility Entity Tests")
class FacilityTest {

    @Test
    @DisplayName("Builder should create Facility with all fields")
    void builder_CreatesValidFacility() {
        // Given
        UUID facilityId = UUID.randomUUID();
        String facilityName = "Main Warehouse";
        FacilityType facilityType = FacilityType.GOOD;
        boolean extendable = true;

        // When
        Facility facility = Facility.builder()
                .facilityId(facilityId)
                .facilityName(facilityName)
                .facilityType(facilityType)
                .extendable(extendable)
                .build();

        // Then
        assertThat(facility).isNotNull();
        assertThat(facility.getFacilityId()).isEqualTo(facilityId);
        assertThat(facility.getFacilityName()).isEqualTo(facilityName);
        assertThat(facility.getFacilityType()).isEqualTo(facilityType);
        assertThat(facility.isExtendable()).isTrue();
    }

    @Test
    @DisplayName("NoArgsConstructor should create empty Facility")
    void noArgsConstructor_CreatesEmptyFacility() {
        // When
        Facility facility = new Facility();

        // Then
        assertThat(facility).isNotNull();
        assertThat(facility.getFacilityId()).isNull();
        assertThat(facility.getFacilityName()).isNull();
        assertThat(facility.getFacilityType()).isNull();
        assertThat(facility.isExtendable()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor should create Facility with provided values")
    void allArgsConstructor_CreatesValidFacility() {
        // Given
        UUID facilityId = UUID.randomUUID();
        String facilityName = "Storage Unit";
        FacilityType facilityType = FacilityType.BONDS;
        boolean extendable = false;

        // When
        Facility facility = new Facility(facilityId, facilityName, facilityType, extendable);

        // Then
        assertThat(facility.getFacilityId()).isEqualTo(facilityId);
        assertThat(facility.getFacilityName()).isEqualTo(facilityName);
        assertThat(facility.getFacilityType()).isEqualTo(facilityType);
        assertThat(facility.isExtendable()).isFalse();
    }

    @Test
    @DisplayName("Setters should update field values")
    void setters_UpdateFieldValues() {
        // Given
        Facility facility = new Facility();
        UUID newId = UUID.randomUUID();
        String newName = "Updated Facility";
        FacilityType newType = FacilityType.MONEY;

        // When
        facility.setFacilityId(newId);
        facility.setFacilityName(newName);
        facility.setFacilityType(newType);
        facility.setExtendable(true);

        // Then
        assertThat(facility.getFacilityId()).isEqualTo(newId);
        assertThat(facility.getFacilityName()).isEqualTo(newName);
        assertThat(facility.getFacilityType()).isEqualTo(newType);
        assertThat(facility.isExtendable()).isTrue();
    }

    @Test
    @DisplayName("equals and hashCode should work correctly for same facility")
    void equals_SameFacility() {
        // Given
        UUID facilityId = UUID.randomUUID();
        Facility facility1 = Facility.builder()
                .facilityId(facilityId)
                .facilityName("Test")
                .facilityType(FacilityType.GOOD)
                .extendable(true)
                .build();

        Facility facility2 = Facility.builder()
                .facilityId(facilityId)
                .facilityName("Test")
                .facilityType(FacilityType.GOOD)
                .extendable(true)
                .build();

        // Then
        assertThat(facility1).isEqualTo(facility2);
        assertThat(facility1.hashCode()).isEqualTo(facility2.hashCode());
    }

    @Test
    @DisplayName("equals should return false for different facilities")
    void equals_DifferentFacility() {
        // Given
        Facility facility1 = Facility.builder()
                .facilityId(UUID.randomUUID())
                .facilityName("Test1")
                .facilityType(FacilityType.GOOD)
                .extendable(true)
                .build();

        Facility facility2 = Facility.builder()
                .facilityId(UUID.randomUUID())
                .facilityName("Test2")
                .facilityType(FacilityType.BONDS)
                .extendable(false)
                .build();

        // Then
        assertThat(facility1).isNotEqualTo(facility2);
    }

    @Test
    @DisplayName("toString should include all field values")
    void toString_ContainsAllFields() {
        // Given
        UUID facilityId = UUID.randomUUID();
        Facility facility = Facility.builder()
                .facilityId(facilityId)
                .facilityName("Test Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(true)
                .build();

        // When
        String result = facility.toString();

        // Then
        assertThat(result).contains("facilityId=" + facilityId);
        assertThat(result).contains("facilityName=Test Facility");
        assertThat(result).contains("facilityType=MONEY");
        assertThat(result).contains("extendable=true");
    }
}
