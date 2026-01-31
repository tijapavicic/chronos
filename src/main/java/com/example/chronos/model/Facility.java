package com.example.chronos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID facilityId;

    private String facilityName;

    @Enumerated(EnumType.STRING)
    private FacilityType facilityType;

    // Rename boolean field to follow JavaBean conventions so Lombok generates proper getters/setters
    @Column(name = "is_extendable")
    private boolean extendable;
}
