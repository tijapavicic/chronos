package com.example.chronos.controller;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.FacilityType;
import com.example.chronos.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping
    public ResponseEntity<FacilityDTO> createFacility(@RequestBody FacilityDTO dto) {
        return ResponseEntity.ok(facilityService.createFacility(dto));
    }

    @GetMapping
    public ResponseEntity<List<FacilityDTO>> getAllFacilities() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<FacilityDTO>> getFacilitiesByName(@PathVariable String name) {
        return ResponseEntity.ok(facilityService.getFacilitiesByName(name));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<FacilityDTO>> getFacilitiesByType(@PathVariable FacilityType type) {
        return ResponseEntity.ok(facilityService.getFacilitiesByType(type));
    }
}
