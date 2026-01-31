package com.example.chronos.repository;

import com.example.chronos.model.Facility;
import com.example.chronos.model.FacilityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {
    List<Facility> findByFacilityName(String facilityName);

    List<Facility> findByFacilityType(FacilityType facilityType);
}
