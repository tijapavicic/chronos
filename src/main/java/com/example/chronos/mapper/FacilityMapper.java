package com.example.chronos.mapper;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.Facility;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FacilityMapper {
    FacilityDTO toDto(Facility facility);
    Facility toEntity(FacilityDTO dto);
}
