package com.example.chronos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"swagger.enabled=true"})
public class OpenApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void openApiJsonIsServedAndContainsPaths() throws Exception {
        String url = "http://localhost:" + port + "/v3/api-docs";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = resp.getBody();
        assertThat(body).isNotNull();

        JsonNode root = mapper.readTree(body);
        assertThat(root.has("openapi")).isTrue();
        assertThat(root.has("paths")).isTrue();
        JsonNode paths = root.path("paths");
        // Check that at least the ping endpoint appears
        assertThat(paths.has("/simulation/ping")).isTrue();
    }
}
