package com.example.chronos.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RuntimePropertySourceUpdater {

    private final ConfigurableEnvironment env;

    public RuntimePropertySourceUpdater(ApplicationContext ctx) {
        this.env = (ConfigurableEnvironment) ctx.getEnvironment();
    }

    public void addOrReplaceProperty(String key, String value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        MapPropertySource mps = new MapPropertySource("runtimeOverrides", map);
        // Remove if exists
        if (env.getPropertySources().contains("runtimeOverrides")) {
            env.getPropertySources().replace("runtimeOverrides", mps);
        } else {
            env.getPropertySources().addFirst(mps);
        }
    }
}
