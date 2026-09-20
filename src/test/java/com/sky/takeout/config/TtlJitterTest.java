package com.sky.takeout.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TtlJitterTest {

    @Test
    void shouldKeepTtlWhenJitterIsZero() {
        TtlJitter ttlJitter = new TtlJitter(Duration.ZERO);

        assertEquals(Duration.ofMinutes(10), ttlJitter.apply(Duration.ofMinutes(10)));
    }

    @Test
    void shouldKeepTtlWhenJitterIsNegative() {
        TtlJitter ttlJitter = new TtlJitter(Duration.ofSeconds(-5));

        assertEquals(Duration.ofMinutes(10), ttlJitter.apply(Duration.ofMinutes(10)));
    }

    @Test
    void shouldKeepNullAndZeroTtl() {
        TtlJitter ttlJitter = new TtlJitter(Duration.ofSeconds(30));

        assertNull(ttlJitter.apply(null));
        assertEquals(Duration.ZERO, ttlJitter.apply(Duration.ZERO));
    }

    @Test
    void shouldAddJitterWithinBounds() {
        Duration baseTtl = Duration.ofMinutes(10);
        Duration maxJitter = Duration.ofSeconds(30);
        TtlJitter ttlJitter = new TtlJitter(maxJitter);

        Set<Long> observed = new HashSet<>();
        for (int index = 0; index < 200; index++) {
            Duration jittered = ttlJitter.apply(baseTtl);

            assertTrue(jittered.compareTo(baseTtl) >= 0);
            assertTrue(jittered.compareTo(baseTtl.plus(maxJitter)) <= 0);
            observed.add(jittered.toMillis());
        }

        assertTrue(observed.size() > 1);
    }
}
