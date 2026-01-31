package com.example.chronos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulationController {

    @GetMapping("/simulation/ping")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }
}
