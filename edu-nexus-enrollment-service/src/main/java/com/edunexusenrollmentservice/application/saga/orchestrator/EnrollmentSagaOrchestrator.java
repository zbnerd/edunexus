package com.edunexusenrollmentservice.application.saga.orchestrator;

import com.edunexusenrollmentservice.application.saga.event.EnrollmentSagaEvent;
import com.edunexusenrollmentservice.application.saga.step.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the course enrollment saga pattern.
 * Coordinates multiple steps with compensating transactions for rollback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentSagaOrchestrator {

    private final ValidateCourseStep validateCourseStep;
    private final CreatePaymentStep createPaymentStep;
    private final CreateEnrollmentStep createEnrollmentStep;
    private final UpdateCourseCapacityStep updateCourseCapacityStep;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Execute the complete enrollment saga for a user enrolling in a course.
     *
     * @param userId The user ID
     * @param courseId The course ID
     * @return SagaExecutionResult containing the outcome
     */
    @Transactional
    public SagaExecutionResult executeEnrollmentSaga(Long userId, Long courseId) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Starting enrollment saga: sagaId={}, userId={}, courseId={}",
                sagaId, userId, courseId);

        SagaContext context = SagaContext.builder()
                .sagaId(sagaId)
                .userId(userId)
                .courseId(courseId)
                .build();

        // Define saga steps in execution order
        List<SagaStep> steps = List.of(
                validateCourseStep,
                createPaymentStep,
                createEnrollmentStep,
                updateCourseCapacityStep
        );

        // Track successfully completed steps for compensation
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            // Publish saga started event
            publishEvent(EnrollmentSagaEvent.create(
                    sagaId,
                    EnrollmentSagaEvent.SagaEventType.SAGA_STARTED,
                    EnrollmentSagaEvent.SagaStep.VALIDATE_COURSE,
                    EnrollmentSagaEvent.SagaStatus.STARTED,
                    userId,
                    courseId
            ));

            // Execute each step in sequence
            for (SagaStep step : steps) {
                log.info("Executing step: {} for sagaId: {}", step.getStepName(), sagaId);

                SagaStep.StepExecutionResult result = step.execute(context);

                if (!result.success()) {
                    // Step failed - initiate compensation
                    log.error("Step failed: {}, message: {}", step.getStepName(), result.message());
                    compensate(completedSteps, context, result.message());
                    return SagaExecutionResult.failure(
                            sagaId,
                            "Saga failed at step: " + step.getStepName() + ": " + result.message()
                    );
                }

                // Mark step as completed
                completedSteps.add(step);

                // Publish step completed event
                EnrollmentSagaEvent.SagaStep eventStep = mapStepToEventStep(step.getStepName());
                publishEvent(EnrollmentSagaEvent.create(
                        sagaId,
                        EnrollmentSagaEvent.SagaEventType.STEP_COMPLETED,
                        eventStep,
                        EnrollmentSagaEvent.SagaStatus.IN_PROGRESS,
                        userId,
                        courseId
                ));
            }

            // All steps completed successfully
            log.info("Enrollment saga completed successfully: sagaId={}", sagaId);
            publishEvent(EnrollmentSagaEvent.create(
                    sagaId,
                    EnrollmentSagaEvent.SagaEventType.SAGA_COMPLETED,
                    EnrollmentSagaEvent.SagaStep.NOTIFY_USER,
                    EnrollmentSagaEvent.SagaStatus.COMPLETED,
                    userId,
                    courseId
            ));

            return SagaExecutionResult.success(
                    sagaId,
                    context.getEnrollmentId(),
                    context.getPaymentId()
            );

        } catch (Exception e) {
            log.error("Unexpected error during saga execution: sagaId={}", sagaId, e);
            compensate(completedSteps, context, e.getMessage());
            return SagaExecutionResult.failure(
                    sagaId,
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    /**
     * Compensate completed steps in reverse order.
     *
     * @param completedSteps List of steps that completed successfully
     * @param context The saga context
     * @param errorMessage The error message that triggered compensation
     */
    private void compensate(List<SagaStep> completedSteps, SagaContext context, String errorMessage) {
        log.info("Starting compensation for sagaId: {}, steps to compensate: {}",
                context.getSagaId(), completedSteps.size());

        // Publish compensation started event
        publishEvent(EnrollmentSagaEvent.createCompensation(
                context.getSagaId(),
                EnrollmentSagaEvent.SagaStep.VALIDATE_COURSE,
                context.getEnrollmentId(),
                context.getPaymentId(),
                errorMessage
        ));

        // Compensate in reverse order
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = completedSteps.get(i);
            log.info("Compensating step: {}", step.getStepName());
            try {
                step.compensate(context);
            } catch (Exception e) {
                log.error("Failed to compensate step: {}", step.getStepName(), e);
                // Continue compensating remaining steps despite failures
            }
        }

        log.info("Compensation completed for sagaId: {}", context.getSagaId());
    }

    /**
     * Publish saga event for tracking and potential async processing.
     * Uses Spring's ApplicationEventPublisher for decoupled event handling.
     *
     * @param event The saga event to publish
     */
    private void publishEvent(EnrollmentSagaEvent event) {
        try {
            eventPublisher.publishEvent(event);
            log.debug("Published saga event: eventType={}, sagaId={}",
                    event.getEventType(), event.getSagaId());
        } catch (Exception e) {
            log.error("Failed to publish saga event: eventType={}, sagaId={}",
                    event.getEventType(), event.getSagaId(), e);
            // Event publishing failure should not fail the saga
        }
    }

    private EnrollmentSagaEvent.SagaStep mapStepToEventStep(String stepName) {
        return switch (stepName) {
            case "ValidateCourse" -> EnrollmentSagaEvent.SagaStep.VALIDATE_COURSE;
            case "CreatePayment" -> EnrollmentSagaEvent.SagaStep.CREATE_PAYMENT;
            case "CreateEnrollment" -> EnrollmentSagaEvent.SagaStep.CREATE_ENROLLMENT;
            case "UpdateCourseCapacity" -> EnrollmentSagaEvent.SagaStep.UPDATE_COURSE_CAPACITY;
            default -> EnrollmentSagaEvent.SagaStep.VALIDATE_COURSE;
        };
    }

    /**
     * Result of saga execution.
     */
    public record SagaExecutionResult(
            boolean success,
            String sagaId,
            Long enrollmentId,
            Long paymentId,
            String message
    ) {
        public static SagaExecutionResult success(String sagaId, Long enrollmentId, Long paymentId) {
            return new SagaExecutionResult(true, sagaId, enrollmentId, paymentId, null);
        }

        public static SagaExecutionResult failure(String sagaId, String message) {
            return new SagaExecutionResult(false, sagaId, null, null, message);
        }
    }
}
