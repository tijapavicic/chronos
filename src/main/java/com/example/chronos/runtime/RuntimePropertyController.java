package com.example.chronos.runtime;

import com.example.chronos.config.DemoProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RuntimePropertyController {

    private final ConfigurableEnvironment env;
    private final DemoProperties demoProperties;

    public RuntimePropertyController(ConfigurableEnvironment env, DemoProperties demoProperties) {
        this.env = env;
        this.demoProperties = demoProperties;
    }

    @GetMapping("/runtime/demo")
    public String getDemoMessage() {
        return demoProperties.getMessage();
    }

    @PostMapping("/runtime/demo")
    public String setDemoMessage(@RequestParam("message") String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("demo.message", message);
        MapPropertySource ps = new MapPropertySource("runtimeOverrides", map);
        MutablePropertySources sources = env.getPropertySources();
        if (sources.contains("runtimeOverrides")) {
            sources.replace("runtimeOverrides", ps);
        } else {
            sources.addFirst(ps);
        }
        // Manually bind into existing instance so @ConfigurationProperties bean picks up changes
        Binder.get(env).bind("demo", Bindable.ofInstance(demoProperties));
        return demoProperties.getMessage();
    }
}
