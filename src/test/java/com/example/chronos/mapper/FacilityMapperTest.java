package com.example.chronos.mapper;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FacilityMapperTest {

    private final FacilityMapper mapper = Mappers.getMapper(FacilityMapper.class);

    @Test
    void toDtoAndBack() {
        UUID id = UUID.randomUUID();

        Facility entity = Facility.builder()
                .facilityId(id)
                .facilityName("Test Facility")
                .facilityType(FacilityType.MONEY)
                .extendable(true)
                .build();

        FacilityDTO dto = mapper.toDto(entity);
        assertNotNull(dto, "DTO should not be null");
        assertEquals(id, dto.getFacilityId(), "facilityId should be mapped to DTO");
        assertEquals("Test Facility", dto.getFacilityName(), "facilityName should be mapped to DTO");
        assertEquals(FacilityType.MONEY, dto.getFacilityType(), "facilityType should be mapped to DTO");
        assertTrue(dto.isExtendable(), "extendable should be true on DTO");

        Facility back = mapper.toEntity(dto);
        assertNotNull(back, "Entity should not be null after mapping from DTO");
        assertEquals(dto.getFacilityId(), back.getFacilityId(), "facilityId should round-trip");
        assertEquals(dto.getFacilityName(), back.getFacilityName(), "facilityName should round-trip");
        assertEquals(dto.getFacilityType(), back.getFacilityType(), "facilityType should round-trip");
        assertEquals(dto.isExtendable(), back.isExtendable(), "extendable should round-trip");
    }
}
