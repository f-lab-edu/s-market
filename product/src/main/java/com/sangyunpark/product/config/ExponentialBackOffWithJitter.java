package com.sangyunpark.product.config;

import java.util.Random;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;

public class ExponentialBackOffWithJitter implements BackOff {

    private final double multiplier;
    private final long maxElapsedTime;
    private long currentInterval;
    private long elapsedTime;
    private final Random random = new Random();

    public ExponentialBackOffWithJitter(long initialInterval, double multiplier, long maxElapsedTime) {
        this.multiplier = multiplier;
        this.maxElapsedTime = maxElapsedTime;
        this.currentInterval = initialInterval;
        this.elapsedTime = 0;
    }

    @Override
    public BackOffExecution start() {
        return () -> {
            if (elapsedTime >= maxElapsedTime) {
                return BackOffExecution.STOP;
            }

            long next = currentInterval;
            long jittered = (next > 0) ? random.nextLong(next + 1) : 0;

            currentInterval = Math.min((long) (currentInterval * multiplier), Long.MAX_VALUE / 2);

            elapsedTime += jittered;

            return jittered;
        };
    }
}