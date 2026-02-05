package com.example.chronos.dto;

import com.example.chronos.model.FacilityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FacilityDTO Tests")
class FacilityDTOTest {

    @Test
    @DisplayName("Builder should create FacilityDTO with all fields")
    void builder_CreatesValidDTO() {
        // Given
        UUID facilityId = UUID.randomUUID();
        String facilityName = "Test Facility";
        FacilityType facilityType = FacilityType.GOOD;
        boolean extendable = true;

        // When
        FacilityDTO dto = FacilityDTO.builder()
                .facilityId(facilityId)
                .facilityName(facilityName)
                .facilityType(facilityType)
                .extendable(extendable)
                .build();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getFacilityId()).isEqualTo(facilityId);
        assertThat(dto.getFacilityName()).isEqualTo(facilityName);
        assertThat(dto.getFacilityType()).isEqualTo(facilityType);
        assertThat(dto.isExtendable()).isTrue();
    }

    @Test
    @DisplayName("NoArgsConstructor should create empty DTO")
    void noArgsConstructor_CreatesEmptyDTO() {
        // When
        FacilityDTO dto = new FacilityDTO();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getFacilityId()).isNull();
        assertThat(dto.getFacilityName()).isNull();
        assertThat(dto.getFacilityType()).isNull();
        assertThat(dto.isExtendable()).isFalse();
    }

    @Test
    @DisplayName("AllArgsConstructor should create DTO with provided values")
    void allArgsConstructor_CreatesValidDTO() {
        // Given
        UUID facilityId = UUID.randomUUID();
        String facilityName = "Storage";
        FacilityType facilityType = FacilityType.BONDS;
        boolean extendable = false;

        // When
        FacilityDTO dto = new FacilityDTO(facilityId, facilityName, facilityType, extendable);

        // Then
        assertThat(dto.getFacilityId()).isEqualTo(facilityId);
        assertThat(dto.getFacilityName()).isEqualTo(facilityName);
        assertThat(dto.getFacilityType()).isEqualTo(facilityType);
        assertThat(dto.isExtendable()).isFalse();
    }

    @Test
    @DisplayName("Setters should update field values")
    void setters_UpdateFieldValues() {
        // Given
        FacilityDTO dto = new FacilityDTO();
        UUID newId = UUID.randomUUID();
        String newName = "Updated Name";
        FacilityType newType = FacilityType.MONEY;

        // When
        dto.setFacilityId(newId);
        dto.setFacilityName(newName);
        dto.setFacilityType(newType);
        dto.setExtendable(true);

        // Then
        assertThat(dto.getFacilityId()).isEqualTo(newId);
        assertThat(dto.getFacilityName()).isEqualTo(newName);
        assertThat(dto.getFacilityType()).isEqualTo(newType);
        assertThat(dto.isExtendable()).isTrue();
    }

    @Test
    @DisplayName("equals should work correctly for same DTO")
    void equals_SameDTO() {
        // Given
        UUID facilityId = UUID.randomUUID();
        FacilityDTO dto1 = FacilityDTO.builder()
                .facilityId(facilityId)
                .facilityName("Test")
                .facilityType(FacilityType.GOOD)
                .extendable(true)
                .build();

        FacilityDTO dto2 = FacilityDTO.builder()
                .facilityId(facilityId)
                .facilityName("Test")
                .facilityType(FacilityType.GOOD)
                .extendable(true)
                .build();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("equals should return false for different DTOs")
    void equals_DifferentDTO() {
        // Given
        FacilityDTO dto1 = FacilityDTO.builder()
                .facilityId(UUID.randomUUID())
                .facilityName("Test1")
                .facilityType(FacilityType.GOOD)
                .extendable(true)
                .build();

        FacilityDTO dto2 = FacilityDTO.builder()
                .facilityId(UUID.randomUUID())
                .facilityName("Test2")
                .facilityType(FacilityType.BONDS)
                .extendable(false)
                .build();

        // Then
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    @DisplayName("toString should include all field values")
    void toString_ContainsAllFields() {
        // Given
        UUID facilityId = UUID.randomUUID();
        FacilityDTO dto = FacilityDTO.builder()
                .facilityId(facilityId)
                .facilityName("Test DTO")
                .facilityType(FacilityType.MONEY)
                .extendable(true)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains("facilityId=" + facilityId);
        assertThat(result).contains("facilityName=Test DTO");
        assertThat(result).contains("facilityType=MONEY");
        assertThat(result).contains("extendable=true");
    }
}
