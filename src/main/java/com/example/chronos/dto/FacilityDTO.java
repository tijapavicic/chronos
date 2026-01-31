package com.example.chronos.dto;

import com.example.chronos.model.FacilityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDTO {
    private UUID facilityId;
    private String facilityName;
    private FacilityType facilityType;
    private boolean isExtendable;
}
