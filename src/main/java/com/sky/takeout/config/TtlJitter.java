package com.sky.takeout.config;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class TtlJitter {

    private final Duration maxJitter;

    public TtlJitter(Duration maxJitter) {
        this.maxJitter = maxJitter == null || maxJitter.isNegative()
                ? Duration.ZERO
                : maxJitter;
    }

    public Duration apply(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative() || maxJitter.isZero()) {
            return ttl;
        }

        long jitterMillis = ThreadLocalRandom
                .current()
                .nextLong(maxJitter.toMillis() + 1);
        return ttl.plusMillis(jitterMillis);
    }
}
