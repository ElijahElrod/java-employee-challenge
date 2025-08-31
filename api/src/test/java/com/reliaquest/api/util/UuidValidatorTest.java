package com.reliaquest.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UuidValidator}
 */
public class UuidValidatorTest {

    private static final String VALID_UUID_STR = "67050f6d-c2a6-4a59-be61-a8479af074ba";
    private static final String INVALID_UUID_STR = "notvalid";

    @Test
    public void testIsValidUuid() {
        Assertions.assertTrue(UuidValidator.isValidUuid(VALID_UUID_STR));
    }

    @Test
    public void testIsNotValidUuid() {
        Assertions.assertFalse(UuidValidator.isValidUuid(INVALID_UUID_STR));
    }
}
