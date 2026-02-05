package com.example.chronos.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FacilityType Enum Tests")
class FacilityTypeTest {

    @Test
    @DisplayName("Should have exactly three enum values")
    void shouldHaveThreeValues() {
        FacilityType[] values = FacilityType.values();
        assertThat(values).hasSize(3);
    }

    @Test
    @DisplayName("Should contain GOOD type")
    void shouldContainGood() {
        assertThat(FacilityType.valueOf("GOOD")).isEqualTo(FacilityType.GOOD);
    }

    @Test
    @DisplayName("Should contain BONDS type")
    void shouldContainBonds() {
        assertThat(FacilityType.valueOf("BONDS")).isEqualTo(FacilityType.BONDS);
    }

    @Test
    @DisplayName("Should contain MONEY type")
    void shouldContainMoney() {
        assertThat(FacilityType.valueOf("MONEY")).isEqualTo(FacilityType.MONEY);
    }

    @ParameterizedTest
    @EnumSource(FacilityType.class)
    @DisplayName("Each enum value should have correct name")
    void eachValueShouldHaveCorrectName(FacilityType type) {
        assertThat(type.name()).isIn("GOOD", "BONDS", "MONEY");
    }

    @ParameterizedTest
    @EnumSource(FacilityType.class)
    @DisplayName("valueOf should return correct enum for each type")
    void valueOfShouldReturnCorrectEnum(FacilityType type) {
        FacilityType result = FacilityType.valueOf(type.name());
        assertThat(result).isEqualTo(type);
    }

    @Test
    @DisplayName("Enum values should be in correct order")
    void valuesShouldBeInCorrectOrder() {
        FacilityType[] values = FacilityType.values();
        assertThat(values[0]).isEqualTo(FacilityType.GOOD);
        assertThat(values[1]).isEqualTo(FacilityType.BONDS);
        assertThat(values[2]).isEqualTo(FacilityType.MONEY);
    }

    @Test
    @DisplayName("Enum ordinal values should be correct")
    void ordinalValuesShouldBeCorrect() {
        assertThat(FacilityType.GOOD.ordinal()).isEqualTo(0);
        assertThat(FacilityType.BONDS.ordinal()).isEqualTo(1);
        assertThat(FacilityType.MONEY.ordinal()).isEqualTo(2);
    }
}
