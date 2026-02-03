package com.example.chronos.runtime;

import com.example.chronos.config.DemoProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/runtime")
public class RuntimePropertyController {

    private static final String RUNTIME_OVERRIDES = "runtimeOverrides";

    private final ConfigurableEnvironment env;
    private final DemoProperties demoProperties;

    public RuntimePropertyController(ConfigurableEnvironment env, DemoProperties demoProperties) {
        this.env = env;
        this.demoProperties = demoProperties;
    }

    @GetMapping("/demo")
    public String getDemoMessage() {
        return demoProperties.getMessage();
    }

    @PostMapping("/demo")
    public String setDemoMessage(@RequestParam("message") String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("demo.message", message);
        MapPropertySource ps = new MapPropertySource(RUNTIME_OVERRIDES, map);
        MutablePropertySources sources = env.getPropertySources();
        if (sources.contains(RUNTIME_OVERRIDES)) {
            sources.replace(RUNTIME_OVERRIDES, ps);
        } else {
            sources.addFirst(ps);
        }
        // Manually bind into existing instance so @ConfigurationProperties bean picks up changes
        Binder.get(env).bind("demo", Bindable.ofInstance(demoProperties));
        return demoProperties.getMessage();
    }

    @GetMapping(path = "/properties", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> properties() {
        return Map.of();
    }
}
