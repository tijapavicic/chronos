package com.example.chronos.controller;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.FacilityType;
import com.example.chronos.service.FacilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacilityController Unit Tests")
class FacilityControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private FacilityService facilityService;

    @InjectMocks
    private FacilityController facilityController;

    private ObjectMapper objectMapper;
    private FacilityDTO facilityDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(facilityController).build();

        UUID facilityId = UUID.randomUUID();
        facilityDTO = FacilityDTO.builder()
                .facilityId(facilityId)
                .facilityName("Test Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(true)
                .build();
    }

    @Test
    @DisplayName("createFacility - should return created facility")
    void createFacility_ReturnsCreatedFacility() throws Exception {
        // Given
        when(facilityService.createFacility(any(FacilityDTO.class))).thenReturn(facilityDTO);

        // When / Then
        mockMvc.perform(post("/api/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facilityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilityName").value("Test Facility"))
                .andExpect(jsonPath("$.facilityType").value("MONEY"))
                .andExpect(jsonPath("$.extendable").value(true));
    }

    @Test
    @DisplayName("getAllFacilities - should return list of facilities")
    void getAllFacilities_ReturnsList() throws Exception {
        // Given
        List<FacilityDTO> facilities = Arrays.asList(facilityDTO);
        when(facilityService.getAllFacilities()).thenReturn(facilities);

        // When / Then
        mockMvc.perform(get("/api/facilities")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityName").value("Test Facility"));
    }

    @Test
    @DisplayName("getFacilitiesByName - should return facilities matching name")
    void getFacilitiesByName_ReturnsMatchingFacilities() throws Exception {
        // Given
        String searchName = "Test Facility";
        List<FacilityDTO> facilities = Arrays.asList(facilityDTO);
        when(facilityService.getFacilitiesByName(searchName)).thenReturn(facilities);

        // When / Then
        mockMvc.perform(get("/api/facilities/{name}", searchName)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityName").value(searchName));
    }

    @Test
    @DisplayName("getFacilitiesByType - should return facilities of specified type")
    void getFacilitiesByType_ReturnsMatchingFacilities() throws Exception {
        // Given
        FacilityType type = FacilityType.MONEY;
        List<FacilityDTO> facilities = Arrays.asList(facilityDTO);
        when(facilityService.getFacilitiesByType(type)).thenReturn(facilities);

        // When / Then
        mockMvc.perform(get("/api/facilities/type/{type}", type)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityType").value("MONEY"));
    }
}
