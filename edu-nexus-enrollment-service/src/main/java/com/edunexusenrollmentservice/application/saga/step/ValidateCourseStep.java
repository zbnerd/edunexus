package com.edunexusenrollmentservice.application.saga.step;

import com.edunexusenrollmentservice.domain.grpc.CourseServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Saga step to validate that the course exists and is available for enrollment.
 * Compensation: None (read-only operation)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateCourseStep implements SagaStep {

    private final CourseServiceClient courseServiceClient;

    @Override
    public StepExecutionResult execute(SagaContext context) {
        log.info("Executing ValidateCourseStep for sagaId: {}, courseId: {}",
                context.getSagaId(), context.getCourseId());

        try {
            // Validate course exists via gRPC call to course service
            boolean courseExists = courseServiceClient.validateCourse(context.getCourseId());

            if (!courseExists) {
                String errorMsg = String.format("Course not found or unavailable: courseId=%d", context.getCourseId());
                log.error(errorMsg);
                return StepExecutionResult.failure(errorMsg);
            }

            log.info("Course validated successfully: courseId={}", context.getCourseId());
            return StepExecutionResult.success("Course validated successfully");

        } catch (Exception e) {
            log.error("Failed to validate course: courseId={}", context.getCourseId(), e);
            return StepExecutionResult.failure("Course validation failed: " + e.getMessage());
        }
    }

    @Override
    public void compensate(SagaContext context) {
        // No compensation needed for read-only validation
        log.info("ValidateCourseStep compensation (no-op) for sagaId: {}", context.getSagaId());
    }

    @Override
    public String getStepName() {
        return "ValidateCourse";
    }
}
