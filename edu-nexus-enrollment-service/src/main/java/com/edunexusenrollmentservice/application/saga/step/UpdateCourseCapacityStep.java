package com.edunexusenrollmentservice.application.saga.step;

import com.edunexusenrollmentservice.domain.grpc.CourseServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Saga step to update course capacity/availability.
 * Compensation: Increment capacity back (undo the decrement)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCourseCapacityStep implements SagaStep {

    private final CourseServiceClient courseServiceClient;

    @Override
    public StepExecutionResult execute(SagaContext context) {
        log.info("Executing UpdateCourseCapacityStep for sagaId: {}, courseId: {}",
                context.getSagaId(), context.getCourseId());

        try {
            // Notify course service to decrement available capacity
            boolean updated = courseServiceClient.updateCourseCapacity(
                    context.getCourseId(),
                    -1  // Decrement by 1
            );

            if (!updated) {
                String errorMsg = String.format("Failed to update course capacity: courseId=%d", context.getCourseId());
                log.error(errorMsg);
                return StepExecutionResult.failure(errorMsg);
            }

            log.info("Course capacity updated successfully: courseId={}", context.getCourseId());
            return StepExecutionResult.success("Course capacity updated successfully");

        } catch (Exception e) {
            log.error("Failed to update course capacity: courseId={}", context.getCourseId(), e);
            return StepExecutionResult.failure("Course capacity update failed: " + e.getMessage());
        }
    }

    @Override
    public void compensate(SagaContext context) {
        log.info("Compensating UpdateCourseCapacityStep: incrementing capacity for courseId={}",
                context.getCourseId());

        try {
            // Increment capacity back (undo the decrement)
            courseServiceClient.updateCourseCapacity(context.getCourseId(), 1);
            log.info("Course capacity compensated successfully: courseId={}", context.getCourseId());
        } catch (Exception e) {
            log.error("Failed to compensate course capacity: courseId={}", context.getCourseId(), e);
        }
    }

    @Override
    public String getStepName() {
        return "UpdateCourseCapacity";
    }
}
