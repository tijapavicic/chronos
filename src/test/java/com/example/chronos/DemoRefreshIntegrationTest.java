package com.example.chronos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoRefreshIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ConfigurableEnvironment env;

    @Test
    void refreshScopeBeanUpdatesAfterActuatorRefresh() throws Exception {
        // initial value must be default
        ResponseEntity<String> r1 = rest.getForEntity("/demo/message", String.class);
        assertThat(r1.getBody()).isNotNull();

        // add runtime override
        org.springframework.core.env.MapPropertySource mps = new org.springframework.core.env.MapPropertySource("testOverride",
                java.util.Collections.singletonMap("demo.message", "new-value"));
        env.getPropertySources().addFirst(mps);

        // before refresh, the controller should still return old value because DemoProperties is @RefreshScope
        ResponseEntity<String> before = rest.getForEntity("/demo/message", String.class);
        assertThat(before.getBody()).isNotEqualTo("new-value");

        // trigger actuator refresh endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> refreshResp = rest.exchange("/actuator/refresh", HttpMethod.POST, entity, String.class);
        assertThat(refreshResp.getStatusCode().is2xxSuccessful()).isTrue();

        // now the controller should reflect the new value
        ResponseEntity<String> after = rest.getForEntity("/demo/message", String.class);
        assertThat(after.getBody()).isEqualTo("new-value");
    }
}
