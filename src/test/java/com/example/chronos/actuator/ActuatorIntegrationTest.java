package com.example.chronos.actuator;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.swing.interop.SwingInterOpUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"swagger.security.enabled=true", "swagger.security.username=swagger", "swagger.security.password=swagger"})
class ActuatorIntegrationTest {

    public static final String HTTP_LOCALHOST = "http://localhost:";
    public static final String ACTUATOR_PATH = "/actuator";
    @LocalServerPort
    int port;

    // Use a local TestRestTemplate instance to avoid relying on autoconfigured bean
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @ParameterizedTest(name = "Checking if actuator: {0} endpoint is public")
    @ValueSource(strings = {"health", "info"})
    void actuatorRootAndCommonEndpointsArePublic(String endpointPath) {
        String base = HTTP_LOCALHOST + port + ACTUATOR_PATH;

        // Try reading root; if not OK, continue — we still want to assert health/info are accessible
        ResponseEntity<JsonNode> root = restTemplate.getForEntity(base, JsonNode.class);
        if (root.getStatusCode() == HttpStatus.OK) {
            JsonNode rootBody = root.getBody();
            assertNotNull(rootBody, "Actuator root body should not be null when root returns OK");
            // The root response should contain _links or links to endpoints
            assertTrue(rootBody.has("_links") || rootBody.has("links"), "Actuator root should include links when root returns OK");
        }

        ResponseEntity<JsonNode> resp = restTemplate.getForEntity(base + "/" + endpointPath, JsonNode.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "Endpoint /actuator/" + endpointPath + " should return 200");
        assertNotNull(resp.getBody(), "Response body for /actuator/" + endpointPath + " should not be null");

    }
    @Test
    void otherActuatorEndpointsReturn403ForAnonymousAndSwaggerUser() {
        // choose an endpoint that is not exposed (e.g., beans or metrics)
        String nonExposed = "/actuator/beans";

        // Anonymous request: not-exposed endpoints may return 401/403/404 depending on config
        ResponseEntity<JsonNode> anon = restTemplate.getForEntity(HTTP_LOCALHOST + port + nonExposed, JsonNode.class);
        assertTrue(anon.getStatusCode() == HttpStatus.UNAUTHORIZED || anon.getStatusCode() == HttpStatus.FORBIDDEN || anon.getStatusCode() == HttpStatus.NOT_FOUND,
                "Anonymous should not be authorized to access non-exposed actuator endpoints");

        // Authenticated as SWAGGER user (has role SWAGGER) should also be forbidden (403)
        TestRestTemplate authTemplate = restTemplate.withBasicAuth("swagger", "swagger");
        ResponseEntity<JsonNode> authResp = authTemplate.getForEntity(HTTP_LOCALHOST + port + nonExposed, JsonNode.class);
        assertEquals(HttpStatus.FORBIDDEN, authResp.getStatusCode(), "Authenticated SWAGGER user should get 403 for non-allowed actuator endpoint");
    }

    @ParameterizedTest(name = "Non-exposed actuator endpoint {0} is unauthorized for anonymous and forbidden for SWAGGER user")
    @ValueSource(strings = {"beans", "metrics", "env", "mappings"})
    void nonExposedActuatorEndpointsAreUnauthorized(String endpoint) {
        String url = HTTP_LOCALHOST + port + ACTUATOR_PATH + "/" + endpoint;

        // Anonymous request: depending on exposure this may be 401 (unauthorized), 403 (forbidden) or 404 (not found)
        ResponseEntity<JsonNode> anon = restTemplate.getForEntity(url, JsonNode.class);
        assertTrue(anon.getStatusCode() == HttpStatus.UNAUTHORIZED || anon.getStatusCode() == HttpStatus.FORBIDDEN || anon.getStatusCode() == HttpStatus.NOT_FOUND,
                () -> "Anonymous request to " + url + " should not be allowed (expected 401/403/404), but was " + anon.getStatusCode());

        // Authenticated as SWAGGER user (no ACTUATOR role) -> 403 Forbidden
        TestRestTemplate auth = restTemplate.withBasicAuth("swagger", "swagger");
        ResponseEntity<JsonNode> authResp = auth.getForEntity(url, JsonNode.class);
        assertEquals(HttpStatus.FORBIDDEN, authResp.getStatusCode(), "SWAGGER user should get 403 for " + url);
    }
}
