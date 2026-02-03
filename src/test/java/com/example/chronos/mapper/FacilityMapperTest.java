package com.example.chronos.mapper;

import com.example.chronos.dto.FacilityDTO;
import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FacilityMapperTest {

    private final FacilityMapper mapper = Mappers.getMapper(FacilityMapper.class);

    static Stream<Arguments> facilityProvider() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), "Test Facility", FacilityType.MONEY, true),
                Arguments.of(UUID.randomUUID(), "Another Facility", FacilityType.GOOD, false),
                Arguments.of(UUID.randomUUID(), "Third Facility", FacilityType.MONEY, false)
        );
    }

    @ParameterizedTest
    @MethodSource("facilityProvider")
    void toDtoAndBack(UUID id, String name, FacilityType type, boolean extendable) {
        Facility entity = Facility.builder()
                .facilityId(id)
                .facilityName(name)
                .facilityType(type)
                .extendable(extendable)
                .build();

        FacilityDTO dto = mapper.toDto(entity);
        assertNotNull(dto, "DTO should not be null");
        assertEquals(id, dto.getFacilityId(), "facilityId should be mapped to DTO");
        assertEquals(name, dto.getFacilityName(), "facilityName should be mapped to DTO");
        assertEquals(type, dto.getFacilityType(), "facilityType should be mapped to DTO");
        assertEquals(extendable, dto.isExtendable(), "extendable should be mapped to DTO");

        Facility back = mapper.toEntity(dto);
        assertNotNull(back, "Entity should not be null after mapping from DTO");
        assertEquals(dto.getFacilityId(), back.getFacilityId(), "facilityId should round-trip");
        assertEquals(dto.getFacilityName(), back.getFacilityName(), "facilityName should round-trip");
        assertEquals(dto.getFacilityType(), back.getFacilityType(), "facilityType should round-trip");
        assertEquals(dto.isExtendable(), back.isExtendable(), "extendable should round-trip");
    }
}
