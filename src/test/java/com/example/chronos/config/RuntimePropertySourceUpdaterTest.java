package com.example.chronos.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuntimePropertySourceUpdater Unit Tests")
class RuntimePropertySourceUpdaterTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private MutablePropertySources propertySources;

    private RuntimePropertySourceUpdater updater;

    @BeforeEach
    void setUp() {
        when(applicationContext.getEnvironment()).thenReturn(environment);
        when(environment.getPropertySources()).thenReturn(propertySources);
        updater = new RuntimePropertySourceUpdater(applicationContext);
    }

    @Test
    @DisplayName("addOrReplaceProperty - should add new property source when not exists")
    void addOrReplaceProperty_AddsNewPropertySource() {
        // Given
        when(propertySources.contains("runtimeOverrides")).thenReturn(false);

        // When
        updater.addOrReplaceProperty("test.key", "test.value");

        // Then
        verify(propertySources).addFirst(any());
        verify(propertySources, never()).replace(anyString(), any());
    }

    @Test
    @DisplayName("addOrReplaceProperty - should replace existing property source")
    void addOrReplaceProperty_ReplacesExistingPropertySource() {
        // Given
        when(propertySources.contains("runtimeOverrides")).thenReturn(true);

        // When
        updater.addOrReplaceProperty("test.key", "test.value");

        // Then
        verify(propertySources).replace(eq("runtimeOverrides"), any());
        verify(propertySources, never()).addFirst(any());
    }

    @Test
    @Disabled
    @DisplayName("Constructor should initialize with environment from context")
    void constructor_InitializesWithEnvironment() {
        // Given / When
        RuntimePropertySourceUpdater newUpdater = new RuntimePropertySourceUpdater(applicationContext);

        // Then
        assertThat(newUpdater).isNotNull();
        verify(applicationContext, atLeast(1)).getEnvironment();
    }
}
