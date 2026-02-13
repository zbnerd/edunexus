package com.edunexuscourseservice.application.service.kafka.dlt;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for Dead Letter Topic (DLT) messages.
 * <p>
 * Creates structured JSON payloads for failed Kafka messages including:
 * - Original message metadata (topic, partition, offset)
 * - Original key and value
 * - Exception information
 * - Timestamp
 */
@Component
public class DltMessageBuilder {

    /**
     * Build a DLT message from a failed consumer record.
     *
     * @param failedRecord The failed Kafka record
     * @param exception The exception that caused the failure
     * @return JSON string representation of the DLT message
     */
    public String buildDltMessage(ConsumerRecord<?, ?> failedRecord, Exception exception) {
        Map<String, Object> dltMessage = new HashMap<>();
        dltMessage.put("originalTopic", failedRecord.topic());
        dltMessage.put("originalPartition", failedRecord.partition());
        dltMessage.put("originalOffset", failedRecord.offset());
        dltMessage.put("originalKey", failedRecord.key());
        dltMessage.put("originalValue", failedRecord.value());
        dltMessage.put("exception", exception.getClass().getName());
        dltMessage.put("exceptionMessage", exception.getMessage());
        dltMessage.put("timestamp", System.currentTimeMillis());

        return toJson(dltMessage);
    }

    /**
     * Convert a map to JSON string.
     * <p>
     * Simple JSON serialization without external dependencies.
     * For production, consider using Jackson ObjectMapper.
     *
     * @param data The data map to serialize
     * @return JSON string
     */
    private String toJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape special characters for JSON.
     *
     * @param value The string value to escape
     * @return Escaped string
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
