package com.example.chronos;

import com.example.chronos.config.RuntimePropertySourceUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RuntimePropertySourceTest {

    @Autowired
    RuntimePropertySourceUpdater updater;

    @Autowired
    ConfigurableEnvironment env;

    @Test
    void programmaticPropertySourceIsVisibleFromEnvironment() {
        updater.addOrReplaceProperty("test.runtime.key", "abc123");
        assertThat(env.getProperty("test.runtime.key")).isEqualTo("abc123");
    }
}
