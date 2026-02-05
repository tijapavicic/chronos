package com.example.chronos.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SimulationController Unit Tests")
class SimulationControllerUnitTest {

    private MockMvc mockMvc;
    private SimulationController controller;

    @BeforeEach
    void setUp() {
        controller = new SimulationController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("ping - should return 200 OK")
    void ping_Returns200() throws Exception {
        // When / Then
        mockMvc.perform(get("/simulation/ping"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ping - should return empty body")
    void ping_ReturnsEmptyBody() throws Exception {
        // When / Then
        mockMvc.perform(get("/simulation/ping"))
                .andExpect(status().isOk());
    }
}
