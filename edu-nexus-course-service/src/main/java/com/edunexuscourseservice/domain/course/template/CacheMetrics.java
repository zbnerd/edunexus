package com.edunexuscourseservice.domain.course.template;

import org.springframework.stereotype.Component;

/**
 * Cache metrics tracking
 * <p>
 * Thread-safe metrics for monitoring cache performance:
 * - Hit rate calculation
 * - Total request tracking
 * - Reset capability for testing
 */
@Component
public class CacheMetrics {
    private volatile long hits = 0;
    private volatile long misses = 0;

    public void recordHit() {
        hits++;
    }

    public void recordMiss() {
        misses++;
    }

    public long getHits() {
        return hits;
    }

    public long getMisses() {
        return misses;
    }

    public long getTotalRequests() {
        return hits + misses;
    }

    public double getHitRate() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) hits / total;
    }

    public void reset() {
        hits = 0;
        misses = 0;
    }

    @Override
    public String toString() {
        return String.format("CacheMetrics{hits=%d, misses=%d, hitRate=%.2f%%}",
                hits, misses, getHitRate() * 100);
    }
}
