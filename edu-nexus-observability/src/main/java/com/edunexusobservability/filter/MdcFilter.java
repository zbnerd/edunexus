package com.edunexusobservability.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC (Mapped Diagnostic Context) Filter
 *
 * Populates MDC with correlation ID and trace ID for structured logging.
 * These values are automatically included in all log statements within the request scope.
 *
 * MDC Fields:
 * - correlationId: Unique ID for tracking related requests across services
 * - traceId: Distributed tracing ID from Spring Cloud Sleuth/OTel
 * - spanId: Current span ID from distributed tracing
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String SPAN_ID_MDC_KEY = "spanId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = getOrGenerateCorrelationId(request);
        String traceId = getTraceId(request);
        String spanId = getSpanId(request);

        try {
            // Populate MDC for this request
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            if (traceId != null) {
                MDC.put(TRACE_ID_MDC_KEY, traceId);
            }
            if (spanId != null) {
                MDC.put(SPAN_ID_MDC_KEY, spanId);
            }

            // Add correlation ID to response for client-side tracking
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            if (log.isTraceEnabled()) {
                log.trace("Request started: {} {} | CorrelationID: {} | TraceID: {}",
                        request.getMethod(), request.getRequestURI(), correlationId, traceId);
            }

            filterChain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(CORRELATION_ID_MDC_KEY);
            MDC.remove(TRACE_ID_MDC_KEY);
            MDC.remove(SPAN_ID_MDC_KEY);
        }
    }

    /**
     * Extract correlation ID from request header or generate a new one
     */
    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = generateShortUuid();
        }
        return correlationId;
    }

    /**
     * Extract trace ID from request headers (populated by Spring Cloud Gateway/OTel)
     */
    private String getTraceId(HttpServletRequest request) {
        // Try Spring Cloud Sleuth/B3 headers
        String traceId = request.getHeader("b3");
        if (traceId != null && !traceId.isBlank()) {
            return traceId.split("-")[0]; // B3 format: traceId-spanId-parentSpanId-1
        }

        // Try W3C Trace Context header
        String traceparent = request.getHeader("traceparent");
        if (traceparent != null && !traceparent.isBlank()) {
            // traceparent format: 00-traceId-spanId-01
            String[] parts = traceparent.split("-");
            if (parts.length >= 2) {
                return parts[1];
            }
        }

        return null;
    }

    /**
     * Extract span ID from request headers
     */
    private String getSpanId(HttpServletRequest request) {
        String b3 = request.getHeader("b3");
        if (b3 != null && !b3.isBlank()) {
            String[] parts = b3.split("-");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return null;
    }

    /**
     * Generate a short UUID for correlation ID (16 chars)
     */
    private String generateShortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}