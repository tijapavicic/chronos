package com.example.chronos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Simulation", description = "Simulation-related endpoints")
public class SimulationController {

    @Operation(summary = "Ping the simulation server", description = "Returns 200 OK when the server is reachable")
    @ApiResponse(responseCode = "200", description = "Server is alive")
    @GetMapping("/simulation/ping")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }
}
