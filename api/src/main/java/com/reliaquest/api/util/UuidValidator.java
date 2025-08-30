package com.reliaquest.api.util;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;

/**
 * Utility class for validating UUIDs
 */
@Log4j2
public class UuidValidator {

    /**
     * Validates if the passed id is a valid UUID.
     *
     * @param id the id to validate as an UUID
     * @return true if valid UUID, false otherwise
     */
    public static boolean isValidUuid(final String id) {
        try {
            UUID.fromString(id);
            return true;
        } catch (final IllegalArgumentException exception) {
            log.warn("Could not create valid UUID from [{}]", id);
            return false;
        }
    }
}
