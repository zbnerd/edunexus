package com.edunexusenrollmentservice.application.saga.coordinator;

import com.edunexusenrollmentservice.application.saga.event.EnrollmentSagaEvent;
import com.edunexusenrollmentservice.application.saga.orchestrator.EnrollmentSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed Transaction Coordinator for managing saga state across services.
 * Tracks transaction lifecycle and handles compensating transactions for rollback.
 *
 * This coordinator:
 * 1. Listens to saga events from Kafka
 * 2. Maintains transaction state
 * 3. Triggers compensating transactions when needed
 * 4. Provides visibility into distributed transaction state
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedTransactionCoordinator {

    // In-memory transaction state tracking
    // In production, this would be backed by a database for durability
    private final Map<String, TransactionState> transactionStates = new ConcurrentHashMap<>();

    /**
     * Listen to saga events and track transaction state.
     * Handles compensation triggering for failed transactions.
     *
     * @param event The saga event
     */
    @EventListener
    public void handleSagaEvent(EnrollmentSagaEvent event) {
        log.info("Received saga event: eventType={}, sagaId={}",
                event.getEventType(), event.getSagaId());

        switch (event.getEventType()) {
            case SAGA_STARTED -> handleSagaStarted(event);
            case STEP_COMPLETED -> handleStepCompleted(event);
            case STEP_FAILED -> handleStepFailed(event);
            case COMPENSATION_STARTED -> handleCompensationStarted(event);
            case COMPENSATION_COMPLETED -> handleCompensationCompleted(event);
            case SAGA_COMPLETED -> handleSagaCompleted(event);
            case SAGA_FAILED -> handleSagaFailed(event);
        }
    }

    private void handleSagaStarted(EnrollmentSagaEvent event) {
        log.info("Saga started: sagaId={}", event.getSagaId());
        transactionStates.put(event.getSagaId(), new TransactionState(
                event.getSagaId(),
                event.getUserId(),
                event.getCourseId(),
                TransactionStatus.STARTED
        ));
    }

    private void handleStepCompleted(EnrollmentSagaEvent event) {
        log.info("Step completed: sagaId={}, step={}",
                event.getSagaId(), event.getCurrentStep());

        TransactionState state = transactionStates.get(event.getSagaId());
        if (state != null) {
            state.addCompletedStep(event.getCurrentStep());
            state.setStatus(TransactionStatus.IN_PROGRESS);
        }
    }

    private void handleStepFailed(EnrollmentSagaEvent event) {
        log.error("Step failed: sagaId={}, step={}, error={}",
                event.getSagaId(), event.getCurrentStep(), event.getErrorMessage());

        TransactionState state = transactionStates.get(event.getSagaId());
        if (state != null) {
            state.setStatus(TransactionStatus.FAILED);
            state.setErrorMessage(event.getErrorMessage());

            // Trigger compensation
            triggerCompensation(event);
        }
    }

    private void handleCompensationStarted(EnrollmentSagaEvent event) {
        log.warn("Compensation started: sagaId={}", event.getSagaId());

        TransactionState state = transactionStates.get(event.getSagaId());
        if (state != null) {
            state.setStatus(TransactionStatus.COMPENSATING);
        }
    }

    private void handleCompensationCompleted(EnrollmentSagaEvent event) {
        log.info("Compensation completed: sagaId={}", event.getSagaId());

        TransactionState state = transactionStates.get(event.getSagaId());
        if (state != null) {
            state.setStatus(TransactionStatus.COMPENSATED);
        }
    }

    private void handleSagaCompleted(EnrollmentSagaEvent event) {
        log.info("Saga completed successfully: sagaId={}", event.getSagaId());

        TransactionState state = transactionStates.get(event.getSagaId());
        if (state != null) {
            state.setStatus(TransactionStatus.COMPLETED);
        }
    }

    private void handleSagaFailed(EnrollmentSagaEvent event) {
        log.error("Saga failed: sagaId={}, error={}",
                event.getSagaId(), event.getErrorMessage());

        TransactionState state = transactionStates.get(event.getSagaId());
        if (state != null) {
            state.setStatus(TransactionStatus.FAILED);
            state.setErrorMessage(event.getErrorMessage());
        }
    }

    /**
     * Trigger compensating transaction for a failed saga.
     * In production, this would send commands to compensation handlers via message queue.
     *
     * @param failedEvent The event that triggered the failure
     */
    private void triggerCompensation(EnrollmentSagaEvent failedEvent) {
        log.warn("Triggering compensation for sagaId: {}", failedEvent.getSagaId());

        // In production with Kafka, this would publish to a compensation topic
        // For now, the compensation is handled directly by the orchestrator
        log.info("Compensation will be handled by orchestrator: sagaId={}",
                failedEvent.getSagaId());
    }

    /**
     * Get transaction state by saga ID.
     *
     * @param sagaId The saga ID
     * @return The transaction state, or null if not found
     */
    public TransactionState getTransactionState(String sagaId) {
        return transactionStates.get(sagaId);
    }

    /**
     * Represents the state of a distributed transaction.
     */
    public static class TransactionState {
        private final String sagaId;
        private final Long userId;
        private final Long courseId;
        private TransactionStatus status;
        private String errorMessage;
        private final java.util.Set<EnrollmentSagaEvent.SagaStep> completedSteps =
                new java.util.HashSet<>();

        public TransactionState(String sagaId, Long userId, Long courseId, TransactionStatus status) {
            this.sagaId = sagaId;
            this.userId = userId;
            this.courseId = courseId;
            this.status = status;
        }

        public void setStatus(TransactionStatus status) {
            this.status = status;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void addCompletedStep(EnrollmentSagaEvent.SagaStep step) {
            completedSteps.add(step);
        }

        public String getSagaId() { return sagaId; }
        public Long getUserId() { return userId; }
        public Long getCourseId() { return courseId; }
        public TransactionStatus getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public java.util.Set<EnrollmentSagaEvent.SagaStep> getCompletedSteps() { return completedSteps; }
    }

    /**
     * Transaction status enumeration.
     */
    public enum TransactionStatus {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED
    }
}
