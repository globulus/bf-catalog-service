package com.shopfront.catalog.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceTest {

    private final Instant jan = Instant.parse("2026-01-01T00:00:00Z");
    private final Instant feb = Instant.parse("2026-02-01T00:00:00Z");

    @Test
    void openPriceIsValidFromItsStart() {
        Price price = new Price(null, new BigDecimal("9.99"), "EUR", jan);

        assertTrue(price.isValidAt(jan));
        assertTrue(price.isValidAt(feb));
        assertFalse(price.isValidAt(jan.minusSeconds(1)));
    }

    @Test
    void closedPriceEndsExclusive() {
        Price price = new Price(null, new BigDecimal("9.99"), "EUR", jan);
        price.setValidTo(feb);

        assertTrue(price.isValidAt(feb.minusSeconds(1)));
        assertFalse(price.isValidAt(feb));
    }
}
