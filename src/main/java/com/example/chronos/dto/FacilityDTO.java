package com.example.chronos.dto;

import com.example.chronos.model.FacilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Facility data transfer object")
public class FacilityDTO {
    @Schema(description = "Unique identifier of the facility", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID facilityId;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Facility name", example = "Main Warehouse", maxLength = 100)
    private String facilityName;

    @NotNull
    @Schema(description = "Type of facility")
    private FacilityType facilityType;

    @Schema(description = "Whether the facility is extendable")
    private boolean extendable;
}
