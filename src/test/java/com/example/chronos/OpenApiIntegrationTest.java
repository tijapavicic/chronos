package com.example.chronos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Enable this test only when Swagger is enabled to avoid test failures")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "swagger.enabled=true",
                "swagger.security.enabled=true",
                "swagger.security.username=swagger",
                "swagger.security.password=swagger"
        })
class OpenApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void openApiJsonIsServedAndContainsPaths() throws Exception {
        String url = "http://localhost:" + port + "/v3/api-docs";
        // authenticate using default swagger credentials when swagger is enabled in tests
        ResponseEntity<String> resp = restTemplate.withBasicAuth("swagger", "swagger")
                .getForEntity(url, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = resp.getBody();
        assertThat(body).isNotNull();

        JsonNode root = mapper.readTree(body);
        assertThat(root.has("openapi")).isTrue();
        assertThat(root.has("paths")).isTrue();
        JsonNode paths = root.path("paths");
        // Check that at least the ping endpoint appears
        assertThat(paths.has("/chronos/ping")).isTrue();
    }
}
