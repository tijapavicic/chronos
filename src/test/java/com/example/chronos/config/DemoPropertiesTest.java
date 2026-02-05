package com.example.chronos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DemoProperties Tests")
class DemoPropertiesTest {

    @Test
    @DisplayName("Default message should be 'default'")
    void defaultMessage() {
        // Given / When
        DemoProperties properties = new DemoProperties();

        // Then
        assertThat(properties.getMessage()).isEqualTo("default");
    }

    @Test
    @DisplayName("setMessage should update message")
    void setMessage_UpdatesMessage() {
        // Given
        DemoProperties properties = new DemoProperties();
        String newMessage = "Updated message";

        // When
        properties.setMessage(newMessage);

        // Then
        assertThat(properties.getMessage()).isEqualTo(newMessage);
    }

    @Test
    @DisplayName("getMessage should return current message value")
    void getMessage_ReturnsCurrentValue() {
        // Given
        DemoProperties properties = new DemoProperties();
        properties.setMessage("Test message");

        // When
        String result = properties.getMessage();

        // Then
        assertThat(result).isEqualTo("Test message");
    }
}
