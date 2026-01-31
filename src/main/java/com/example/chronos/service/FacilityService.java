package com.example.chronos.service;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import com.example.chronos.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityDTO createFacility(FacilityDTO dto) {
        Facility facility = Facility.builder()
                .facilityName(dto.getFacilityName())
                .facilityType(dto.getFacilityType())
                .isExtendable(dto.isExtendable())
                .build();

        Facility saved = facilityRepository.save(facility);
        return mapToDTO(saved);
    }

    public List<FacilityDTO> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<FacilityDTO> getFacilitiesByName(String name) {
        return facilityRepository.findByFacilityName(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<FacilityDTO> getFacilitiesByType(FacilityType type) {
        return facilityRepository.findByFacilityType(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FacilityDTO mapToDTO(Facility facility) {
        return FacilityDTO.builder()
                .facilityId(facility.getFacilityId())
                .facilityName(facility.getFacilityName())
                .facilityType(facility.getFacilityType())
                .isExtendable(facility.isExtendable())
                .build();
    }
}
