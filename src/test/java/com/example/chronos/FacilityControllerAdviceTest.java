package com.example.chronos;

import com.example.chronos.controller.FacilityController;
import com.example.chronos.controller.FacilityControllerAdvice;
import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.FacilityType;
import com.example.chronos.service.FacilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FacilityControllerAdviceTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private FacilityService facilityService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(new FacilityController(facilityService))
                .setControllerAdvice(new FacilityControllerAdvice())
                .setMessageConverters(jacksonConverter)
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException from service returns 404 with message and correlationId header")
    void resourceNotFoundReturns404() throws Exception {
        when(facilityService.createFacility(any(FacilityDTO.class)))
                .thenThrow(new com.example.chronos.exception.ResourceNotFoundException("facility not found"));

        FacilityDTO dto = FacilityDTO.builder()
                .facilityName("x")
                .facilityType(FacilityType.MONEY)
                .extendable(false)
                .build();

        mockMvc.perform(post("/api/facilities")
                        .header("X-Correlation-Id", "abc-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("facility not found"))
                .andExpect(jsonPath("$.code").value("ERR_NOT_FOUND"));
    }

    @Test
    @DisplayName("Generic runtime exception maps to 500 and includes correlationId")
    void runtimeExceptionReturns500() throws Exception {
        when(facilityService.getAllFacilities()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/facilities").header("X-Correlation-Id", "xyz-789"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.code").value("ERR_INTERNAL"));
    }

    @Test
    @DisplayName("Validation errors return 400 and include code and correlationId")
    void validationErrorsReturn400() throws Exception {
        FacilityDTO dto = FacilityDTO.builder()
                .facilityName("")
                .facilityType(FacilityType.MONEY)
                .extendable(false)
                .build();

        mockMvc.perform(post("/api/facilities")
                        .header("X-Correlation-Id", "val-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("ERR_VALIDATION"));
    }
}