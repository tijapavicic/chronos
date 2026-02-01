package com.example.chronos.service;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.mapper.FacilityMapper;
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
    private final FacilityMapper facilityMapper;

    public FacilityDTO createFacility(FacilityDTO dto) {
        Facility facility = facilityMapper.toEntity(dto);

        Facility saved = facilityRepository.save(facility);
        return facilityMapper.toDto(saved);
    }

    public List<FacilityDTO> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(facilityMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FacilityDTO> getFacilitiesByName(String name) {
        return facilityRepository.findByFacilityName(name).stream()
                .map(facilityMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FacilityDTO> getFacilitiesByType(FacilityType type) {
        return facilityRepository.findByFacilityType(type).stream()
                .map(facilityMapper::toDto)
                .collect(Collectors.toList());
    }
}
