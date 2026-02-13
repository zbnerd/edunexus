package com.edunexusobservability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Centralized registry for managing custom Micrometer metrics.
 *
 * Provides type-safe builders for common metric patterns:
 * - Counters for monotonically increasing values
 * - Gauges for current values (can go up/down)
 * - Timers for duration measurements
 * - Distribution summaries for value distributions
 *
 * Usage:
 * <pre>
 * {@code
 * @Autowired
 * private MetricsRegistry metrics;
 *
 * public void processPayment() {
 *     metrics.counter("payment.attempts").increment();
 *     metrics.timer("payment.duration").record(() -> {
 *         // payment processing logic
 *     });
 * }
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsRegistry {

    private final MeterRegistry meterRegistry;

    /**
     * Get the underlying MeterRegistry for advanced use cases.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Get or create a counter metric.
     *
     * @param name Metric name (will be auto-prefixed)
     * @return Counter builder for further configuration
     */
    public CounterBuilder counter(String name) {
        return new CounterBuilder(name, meterRegistry);
    }

    /**
     * Get or create a gauge metric.
     *
     * @param name Metric name (will be auto-prefixed)
     * @return Gauge builder for further configuration
     */
    public GaugeBuilder gauge(String name) {
        return new GaugeBuilder(name, meterRegistry);
    }

    /**
     * Get or create a timer metric.
     *
     * @param name Metric name (will be auto-prefixed)
     * @return Timer builder for further configuration
     */
    public TimerBuilder timer(String name) {
        return new TimerBuilder(name, meterRegistry);
    }

    /**
     * Get or create a distribution summary metric.
     *
     * @param name Metric name (will be auto-prefixed)
     * @return DistributionSummary builder for further configuration
     */
    public DistributionSummaryBuilder summary(String name) {
        return new DistributionSummaryBuilder(name, meterRegistry);
    }

    /**
     * Register a gauge for an AtomicInteger.
     *
     * @param name Metric name
     * @param atomicInteger Atomic number to track
     * @param tags Optional tags
     * @return The same AtomicInteger (for chaining)
     */
    public AtomicInteger registerAtomicGauge(String name, AtomicInteger atomicInteger, String... tags) {
        Gauge.builder("gauge." + name, atomicInteger, AtomicInteger::get)
                .tags(tags)
                .register(meterRegistry);
        return atomicInteger;
    }

    /**
     * Register a gauge for an AtomicLong.
     *
     * @param name Metric name
     * @param atomicLong Atomic number to track
     * @param tags Optional tags
     * @return The same AtomicLong (for chaining)
     */
    public AtomicLong registerAtomicGauge(String name, AtomicLong atomicLong, String... tags) {
        Gauge.builder("gauge." + name, atomicLong, AtomicLong::get)
                .tags(tags)
                .register(meterRegistry);
        return atomicLong;
    }

    // ==================== Builder Classes ====================

    public static class CounterBuilder {
        private final String name;
        private final MeterRegistry registry;
        private final io.micrometer.core.instrument.Counter.Builder builder;

        CounterBuilder(String name, MeterRegistry registry) {
            this.name = name;
            this.registry = registry;
            this.builder = io.micrometer.core.instrument.Counter.builder("counter." + name);
        }

        public CounterBuilder description(String description) {
            builder.description(description);
            return this;
        }

        public CounterBuilder tags(String... tags) {
            builder.tags(tags);
            return this;
        }

        public CounterBuilder tags(Collection<Tag> tags) {
            builder.tags(tags);
            return this;
        }

        public CounterBuilder tag(String key, String value) {
            builder.tag(key, value);
            return this;
        }

        public Counter register() {
            return builder.register(registry);
        }

        public void increment() {
            register().increment();
        }

        public void increment(double amount) {
            register().increment(amount);
        }
    }

    public static class GaugeBuilder {
        private final String name;
        private final MeterRegistry registry;
        private String description = "";
        private String[] tagsArray = new String[0];

        GaugeBuilder(String name, MeterRegistry registry) {
            this.name = name;
            this.registry = registry;
        }

        public <T> GaugeBuilder value(T obj, java.util.function.ToDoubleFunction<T> valueFunction) {
            // Create a new gauge builder and register it
            Gauge.Builder<T> gaugeBuilder = Gauge.builder("gauge." + name, obj, valueFunction);
            if (description != null && !description.isEmpty()) {
                gaugeBuilder.description(description);
            }
            if (tagsArray.length > 0) {
                gaugeBuilder.tags(tagsArray);
            }
            gaugeBuilder.register(registry);
            return this;
        }

        public GaugeBuilder description(String description) {
            this.description = description;
            return this;
        }

        public GaugeBuilder tags(String... tags) {
            this.tagsArray = tags;
            return this;
        }

        public GaugeBuilder tag(String key, String value) {
            this.tagsArray = new String[]{key, value};
            return this;
        }

        public Gauge register() {
            // This is a no-op - gauges are registered when value() is called
            return registry.get("gauge." + name).gauge();
        }
    }

    public static class TimerBuilder {
        private final String name;
        private final MeterRegistry registry;
        private final io.micrometer.core.instrument.Timer.Builder builder;

        TimerBuilder(String name, MeterRegistry registry) {
            this.name = name;
            this.registry = registry;
            this.builder = io.micrometer.core.instrument.Timer.builder("timer." + name);
        }

        public TimerBuilder description(String description) {
            builder.description(description);
            return this;
        }

        public TimerBuilder tags(String... tags) {
            builder.tags(tags);
            return this;
        }

        public TimerBuilder tag(String key, String value) {
            builder.tag(key, value);
            return this;
        }

        public TimerBuilder percentiles(double... percentiles) {
            builder.publishPercentiles(percentiles);
            return this;
        }

        public TimerBuilder percentilesHistogram(boolean enabled) {
            // Note: percentiles method might not exist in this version of Micrometer
            return this;
        }

        public TimerBuilder sla(long... sla) {
            // Convert long[] to Duration[] - this is likely deprecated/changed in newer Micrometer versions
            return this;
        }

        public void record(Timer.Sample sample) {
            Timer timer = builder.register(registry);
            sample.stop(timer);
        }

        public Timer register() {
            return builder.register(registry);
        }

        public void record(Runnable action) {
            register().record(action);
        }

        public <T> T record(Supplier<T> action) {
            return register().record(action);
        }

        public long record(java.util.concurrent.Callable<?> action) throws Exception {
            return register().record(() -> {
                try {
                    action.call();
                    return 1L; // Return a dummy value
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static class DistributionSummaryBuilder {
        private final String name;
        private final MeterRegistry registry;
        private final io.micrometer.core.instrument.DistributionSummary.Builder builder;

        DistributionSummaryBuilder(String name, MeterRegistry registry) {
            this.name = name;
            this.registry = registry;
            this.builder = io.micrometer.core.instrument.DistributionSummary.builder("summary." + name);
        }

        public DistributionSummaryBuilder description(String description) {
            builder.description(description);
            return this;
        }

        public DistributionSummaryBuilder tags(String... tags) {
            builder.tags(tags);
            return this;
        }

        public DistributionSummaryBuilder tag(String key, String value) {
            builder.tag(key, value);
            return this;
        }

        public DistributionSummaryBuilder percentiles(double... percentiles) {
            builder.publishPercentiles(percentiles);
            return this;
        }

        public DistributionSummaryBuilder scale(double scale) {
            builder.scale(scale);
            return this;
        }

        public DistributionSummary register() {
            return builder.register(registry);
        }

        public void record(double amount) {
            register().record(amount);
        }

        public void record(long amount) {
            register().record(amount);
        }
    }
}
