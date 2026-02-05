package com.example.chronos.service;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.mapper.FacilityMapper;
import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import com.example.chronos.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacilityService Unit Tests")
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityMapper facilityMapper;

    @InjectMocks
    private FacilityService facilityService;

    private Facility facility;
    private FacilityDTO facilityDTO;

    @BeforeEach
    void setUp() {
        UUID facilityId = UUID.randomUUID();

        facility = Facility.builder()
                .facilityId(facilityId)
                .facilityName("Test Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(true)
                .build();

        facilityDTO = FacilityDTO.builder()
                .facilityId(facilityId)
                .facilityName("Test Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(true)
                .build();
    }

    @Test
    @DisplayName("createFacility - should map DTO to entity, save, and return mapped DTO")
    void createFacility_Success() {
        // Given
        when(facilityMapper.toEntity(any(FacilityDTO.class))).thenReturn(facility);
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);
        when(facilityMapper.toDto(any(Facility.class))).thenReturn(facilityDTO);

        // When
        FacilityDTO result = facilityService.createFacility(facilityDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFacilityName()).isEqualTo("Test Facility");
        assertThat(result.getFacilityType()).isEqualTo(FacilityType.MONEY);
        assertThat(result.isExtendable()).isTrue();

        verify(facilityMapper).toEntity(facilityDTO);
        verify(facilityRepository).save(facility);
        verify(facilityMapper).toDto(facility);
    }

    @Test
    @DisplayName("getAllFacilities - should return all facilities as DTOs")
    void getAllFacilities_Success() {
        // Given
        List<Facility> facilities = Arrays.asList(facility);
        when(facilityRepository.findAll()).thenReturn(facilities);
        when(facilityMapper.toDto(any(Facility.class))).thenReturn(facilityDTO);

        // When
        List<FacilityDTO> results = facilityService.getAllFacilities();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFacilityName()).isEqualTo("Test Facility");

        verify(facilityRepository).findAll();
        verify(facilityMapper).toDto(facility);
    }

    @Test
    @DisplayName("getAllFacilities - should return empty list when no facilities exist")
    void getAllFacilities_EmptyList() {
        // Given
        when(facilityRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<FacilityDTO> results = facilityService.getAllFacilities();

        // Then
        assertThat(results).isEmpty();
        verify(facilityRepository).findAll();
        verify(facilityMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("getFacilitiesByName - should return facilities matching name")
    void getFacilitiesByName_Success() {
        // Given
        String searchName = "Test Facility";
        List<Facility> facilities = Arrays.asList(facility);
        when(facilityRepository.findByFacilityName(searchName)).thenReturn(facilities);
        when(facilityMapper.toDto(any(Facility.class))).thenReturn(facilityDTO);

        // When
        List<FacilityDTO> results = facilityService.getFacilitiesByName(searchName);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFacilityName()).isEqualTo(searchName);

        verify(facilityRepository).findByFacilityName(searchName);
        verify(facilityMapper).toDto(facility);
    }

    @Test
    @DisplayName("getFacilitiesByName - should return empty list when no match")
    void getFacilitiesByName_NoMatch() {
        // Given
        String searchName = "Non-existent";
        when(facilityRepository.findByFacilityName(searchName)).thenReturn(Collections.emptyList());

        // When
        List<FacilityDTO> results = facilityService.getFacilitiesByName(searchName);

        // Then
        assertThat(results).isEmpty();
        verify(facilityRepository).findByFacilityName(searchName);
        verify(facilityMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("getFacilitiesByType - should return facilities matching type")
    void getFacilitiesByType_Success() {
        // Given
        FacilityType searchType = FacilityType.MONEY;
        List<Facility> facilities = Arrays.asList(facility);
        when(facilityRepository.findByFacilityType(searchType)).thenReturn(facilities);
        when(facilityMapper.toDto(any(Facility.class))).thenReturn(facilityDTO);

        // When
        List<FacilityDTO> results = facilityService.getFacilitiesByType(searchType);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFacilityType()).isEqualTo(searchType);

        verify(facilityRepository).findByFacilityType(searchType);
        verify(facilityMapper).toDto(facility);
    }

    @Test
    @DisplayName("getFacilitiesByType - should return empty list when no match")
    void getFacilitiesByType_NoMatch() {
        // Given
        FacilityType searchType = FacilityType.BONDS;
        when(facilityRepository.findByFacilityType(searchType)).thenReturn(Collections.emptyList());

        // When
        List<FacilityDTO> results = facilityService.getFacilitiesByType(searchType);

        // Then
        assertThat(results).isEmpty();
        verify(facilityRepository).findByFacilityType(searchType);
        verify(facilityMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("getFacilitiesByType - should handle multiple facilities of same type")
    void getFacilitiesByType_MultipleFacilities() {
        // Given
        Facility facility2 = Facility.builder()
                .facilityId(UUID.randomUUID())
                .facilityName("Another Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(false)
                .build();

        FacilityDTO facilityDTO2 = FacilityDTO.builder()
                .facilityId(facility2.getFacilityId())
                .facilityName("Another Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(false)
                .build();

        List<Facility> facilities = Arrays.asList(facility, facility2);
        when(facilityRepository.findByFacilityType(FacilityType.MONEY)).thenReturn(facilities);
        when(facilityMapper.toDto(facility)).thenReturn(facilityDTO);
        when(facilityMapper.toDto(facility2)).thenReturn(facilityDTO2);

        // When
        List<FacilityDTO> results = facilityService.getFacilitiesByType(FacilityType.MONEY);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(FacilityDTO::getFacilityName)
                .containsExactlyInAnyOrder("Test Facility", "Another Facility");

        verify(facilityRepository).findByFacilityType(FacilityType.MONEY);
        verify(facilityMapper, times(2)).toDto(any(Facility.class));
    }
}
