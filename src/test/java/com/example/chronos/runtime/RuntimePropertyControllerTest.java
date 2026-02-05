package com.example.chronos.runtime;

import com.example.chronos.config.DemoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuntimePropertyController Unit Tests")
class RuntimePropertyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private MutablePropertySources propertySources;

    @Mock
    private DemoProperties demoProperties;

    private RuntimePropertyController controller;

    @BeforeEach
    void setUp() {
        controller = new RuntimePropertyController(environment, demoProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("getDemoMessage - should return current demo message")
    void getDemoMessage_ReturnsCurrentMessage() throws Exception {
        // Given
        when(demoProperties.getMessage()).thenReturn("Test message");

        // When / Then
        mockMvc.perform(get("/runtime/demo"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test message"));
    }

    @Test
    @Disabled
    @DisplayName("setDemoMessage - should update and return new message")
    void setDemoMessage_UpdatesAndReturnsNewMessage() throws Exception {
        // Given
        String newMessage = "Updated message";
        when(environment.getPropertySources()).thenReturn(propertySources);
        when(propertySources.contains(anyString())).thenReturn(false);
        when(demoProperties.getMessage()).thenReturn(newMessage);

        // When / Then
        mockMvc.perform(post("/runtime/demo")
                        .param("message", newMessage))
                .andExpect(status().isOk())
                .andExpect(content().string(newMessage));

        verify(propertySources).addFirst(any());
    }

    @Test
    @Disabled
    @DisplayName("setDemoMessage - should replace existing property source")
    void setDemoMessage_ReplacesExistingPropertySource() throws Exception {
        // Given
        String newMessage = "Replaced message";
        when(propertySources.contains("runtimeOverrides")).thenReturn(true);
        when(demoProperties.getMessage()).thenReturn(newMessage);

        // When / Then
        mockMvc.perform(post("/runtime/demo")
                        .param("message", newMessage))
                .andExpect(status().isOk());

        verify(propertySources).replace(eq("runtimeOverrides"), any());
    }

    @Test
    @DisplayName("properties - should return empty map")
    void properties_ReturnsEmptyMap() throws Exception {
        // When / Then
        mockMvc.perform(get("/runtime/properties"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }
}
