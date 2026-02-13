package com.edunexusgraphql.util;

import lombok.Getter;

/**
 * Kafka Topics for Payment and Enrollment Saga Events
 *
 * Implements Saga pattern for distributed transaction coordination:
 * - Payment creation triggers enrollment
 * - Enrollment result triggers payment confirmation or failure
 * - Compensation on failure ensures eventual consistency
 */
@Getter
public enum PaymentKafkaTopic {
    PAYMENT_CREATED("payment-created"),
    PAYMENT_CONFIRMED("payment-confirmed"),
    PAYMENT_FAILED("payment-failed"),
    ENROLLMENT_RESULT("enrollment-result"),
    ENROLLMENT_RESULT_DLT("enrollment-result-dlt");

    private final String topic;

    PaymentKafkaTopic(String topic) {
        this.topic = topic;
    }
}
