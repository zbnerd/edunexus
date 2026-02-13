package com.edunexusgraphql.constants;

import java.time.Duration;

/**
 * Constants for subscription-related calculations.
 */
public final class SubscriptionConstants {

    private SubscriptionConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Default subscription duration: 365 days (1 year).
     */
    public static final long DEFAULT_SUBSCRIPTION_DURATION_MS = Duration.ofDays(365).toMillis();

}
