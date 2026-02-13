package com.edunexusenrollmentservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event representing a state change in the enrollment saga orchestration.
 * Used for tracking saga lifecycle and enabling compensating transactions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentSagaEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp
    private Long sequenceNumber; // Sequence number for ordering

    private String sagaId; // Unique saga instance identifier
    private SagaEventType eventType; // Type of saga event
    private SagaStep currentStep; // Current step in saga flow
    private SagaStatus status; // Current saga status

    private Long userId;
    private Long courseId;
    private Long paymentId;
    private Long enrollmentId;

    private String errorMessage; // Error message if failed
    private String compensationStep; // Step being compensated during rollback

    public static EnrollmentSagaEvent create(String sagaId, SagaEventType eventType,
                                            SagaStep currentStep, SagaStatus status,
                                            Long userId, Long courseId) {
        return EnrollmentSagaEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .sequenceNumber(System.currentTimeMillis())
                .sagaId(sagaId)
                .eventType(eventType)
                .currentStep(currentStep)
                .status(status)
                .userId(userId)
                .courseId(courseId)
                .build();
    }

    public static EnrollmentSagaEvent createCompensation(String sagaId, SagaStep step,
                                                          Long enrollmentId, Long paymentId, String error) {
        return EnrollmentSagaEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .sequenceNumber(System.currentTimeMillis())
                .sagaId(sagaId)
                .eventType(SagaEventType.COMPENSATION)
                .currentStep(step)
                .status(SagaStatus.COMPENSATING)
                .enrollmentId(enrollmentId)
                .paymentId(paymentId)
                .errorMessage(error)
                .build();
    }

    public enum SagaEventType {
        SAGA_STARTED,
        STEP_COMPLETED,
        STEP_FAILED,
        COMPENSATION_STARTED,
        COMPENSATION_COMPLETED,
        SAGA_COMPLETED,
        SAGA_FAILED,
        COMPENSATION  // Added for compensation events
    }

    public enum SagaStatus {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        COMPENSATING,
        COMPENSATED,
        FAILED
    }

    public enum SagaStep {
        VALIDATE_COURSE,
        CREATE_PAYMENT,
        CREATE_ENROLLMENT,
        UPDATE_COURSE_CAPACITY,
        NOTIFY_USER
    }
}
