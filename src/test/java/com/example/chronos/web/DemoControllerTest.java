package com.example.chronos.web;

import com.example.chronos.config.DemoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DemoController Unit Tests")
class DemoControllerTest {

    @Mock
    private DemoProperties demoProperties;

    private DemoController controller;

    @BeforeEach
    void setUp() {
        controller = new DemoController(demoProperties);
    }

    @Test
    @DisplayName("hello - should return hello string")
    void hello_ReturnsHelloString() {
        // When
        String result = controller.hello();

        // Then
        assertThat(result).isEqualTo("hello");
    }

    @Test
    @DisplayName("message - should return message from properties")
    void message_ReturnsMessageFromProperties() {
        // Given
        String expectedMessage = "Test message from properties";
        when(demoProperties.getMessage()).thenReturn(expectedMessage);

        // When
        String result = controller.message();

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("message - should return default message when properties are default")
    void message_ReturnsDefaultMessage() {
        // Given
        when(demoProperties.getMessage()).thenReturn("default");

        // When
        String result = controller.message();

        // Then
        assertThat(result).isEqualTo("default");
    }
}
