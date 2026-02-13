package com.edunexusenrollmentservice.application.saga.step;

import com.edunexusenrollmentservice.domain.entity.Enrollment;
import com.edunexusenrollmentservice.domain.repository.EnrollmentRepository;
import com.edunexusenrollmentservice.domain.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga step to create enrollment record.
 * Compensation: Delete the created enrollment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateEnrollmentStep implements SagaStep {

    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public StepExecutionResult execute(SagaContext context) {
        log.info("Executing CreateEnrollmentStep for sagaId: {}, userId: {}, courseId: {}",
                context.getSagaId(), context.getUserId(), context.getCourseId());

        try {
            if (context.getPaymentId() == null) {
                return StepExecutionResult.failure("PaymentId not found in context");
            }

            // Create enrollment using the paymentId from previous step
            Enrollment enrollment = enrollmentService.registerCourse(
                    context.getUserId(),
                    context.getCourseId(),
                    context.getPaymentId()
            );

            context.setEnrollmentId(enrollment.getEnrollmentId());
            context.put("enrollment", enrollment);

            log.info("Enrollment created successfully: enrollmentId={}", enrollment.getEnrollmentId());
            return StepExecutionResult.success("Enrollment created successfully", enrollment.getEnrollmentId());

        } catch (Exception e) {
            log.error("Failed to create enrollment for userId: {}, courseId: {}",
                    context.getUserId(), context.getCourseId(), e);
            return StepExecutionResult.failure("Enrollment creation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void compensate(SagaContext context) {
        if (context.getEnrollmentId() == null) {
            log.info("No enrollment to compensate for sagaId: {}", context.getSagaId());
            return;
        }

        log.info("Compensating CreateEnrollmentStep: deleting enrollmentId={}",
                context.getEnrollmentId());

        try {
            enrollmentRepository.deleteById(context.getEnrollmentId());
            log.info("Enrollment deleted successfully: enrollmentId={}", context.getEnrollmentId());
        } catch (Exception e) {
            log.error("Failed to delete enrollment during compensation: enrollmentId={}",
                    context.getEnrollmentId(), e);
        }
    }

    @Override
    public String getStepName() {
        return "CreateEnrollment";
    }
}
