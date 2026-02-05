package com.example.chronos.repository;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to conditionally enable tests only when Oracle DB is available.
 * Tests annotated with this will be skipped if Oracle DB cannot be reached.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(OracleAvailableCondition.class)
public @interface EnabledIfOracleAvailable {
    /**
     * Custom reason message for why test was disabled (optional).
     */
    String value() default "Oracle database is not available";
}
