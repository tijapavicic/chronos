package com.example.chronos.controller;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.FacilityType;
import com.example.chronos.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
@Tag(name = "Facilities", description = "Operations related to facility management")
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "Create a new facility")
    @ApiResponse(responseCode = "200", description = "Facility created successfully")
    @PostMapping
    public ResponseEntity<FacilityDTO> createFacility(@RequestBody FacilityDTO dto) {
        return ResponseEntity.ok(facilityService.createFacility(dto));
    }

    @Operation(summary = "Get all facilities")
    @ApiResponse(responseCode = "200", description = "List of facilities")
    @GetMapping
    public ResponseEntity<List<FacilityDTO>> getAllFacilities() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
    }

    @Operation(summary = "Search facilities by name")
    @ApiResponse(responseCode = "200", description = "Facilities matching the provided name")
    @GetMapping("/{name}")
    public ResponseEntity<List<FacilityDTO>> getFacilitiesByName(@PathVariable String name) {
        return ResponseEntity.ok(facilityService.getFacilitiesByName(name));
    }

    @Operation(summary = "Get facilities by type")
    @ApiResponse(responseCode = "200", description = "Facilities of a specific type")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<FacilityDTO>> getFacilitiesByType(@PathVariable FacilityType type) {
        return ResponseEntity.ok(facilityService.getFacilitiesByType(type));
    }
}
