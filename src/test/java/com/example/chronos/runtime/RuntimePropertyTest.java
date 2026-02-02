package com.example.chronos.runtime;

import com.example.chronos.config.DemoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RuntimePropertyTest {

    @Autowired
    ConfigurableEnvironment env;

    @Autowired
    DemoProperties demoProperties;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void manualRebind_shouldUpdateConfigurationProperties() {
        // ensure default
        assertThat(demoProperties.getMessage()).isNotNull();

        Map<String, Object> map = new HashMap<>();
        map.put("demo.message", "from-test");
        MapPropertySource ps = new MapPropertySource("testRuntimeOverrides", map);
        MutablePropertySources sources = env.getPropertySources();
        if (sources.contains("testRuntimeOverrides")) {
            sources.replace("testRuntimeOverrides", ps);
        } else {
            sources.addFirst(ps);
        }

        // rebind
        Binder.get(env).bind("demo", Bindable.ofInstance(demoProperties));

        assertThat(demoProperties.getMessage()).isEqualTo("from-test");

        // also test the controller endpoint
        String body = restTemplate.getForObject("/runtime/demo", String.class);
        assertThat(body).isEqualTo("from-test");
    }
}
