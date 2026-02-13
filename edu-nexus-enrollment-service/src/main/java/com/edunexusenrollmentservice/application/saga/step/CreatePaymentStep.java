package com.edunexusenrollmentservice.application.saga.step;

import com.edunexusenrollmentservice.domain.entity.Payment;
import com.edunexusenrollmentservice.domain.repository.PaymentRepository;
import com.edunexusenrollmentservice.domain.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga step to create payment for course enrollment.
 * Compensation: Delete the created payment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePaymentStep implements SagaStep {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public StepExecutionResult execute(SagaContext context) {
        log.info("Executing CreatePaymentStep for sagaId: {}, userId: {}",
                context.getSagaId(), context.getUserId());

        try {
            // In a real implementation, payment details would come from context
            // For now, we'll create a placeholder payment
            Payment payment = paymentService.createPayment(
                    context.getUserId(),
                    context.getCourseId(),
                    "PLACEHOLDER_AMOUNT"
            );

            context.setPaymentId(payment.getPaymentId());
            context.put("payment", payment);

            log.info("Payment created successfully: paymentId={}", payment.getPaymentId());
            return StepExecutionResult.success("Payment created successfully", payment.getPaymentId());

        } catch (Exception e) {
            log.error("Failed to create payment for userId: {}, courseId: {}",
                    context.getUserId(), context.getCourseId(), e);
            return StepExecutionResult.failure("Payment creation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void compensate(SagaContext context) {
        if (context.getPaymentId() == null) {
            log.info("No payment to compensate for sagaId: {}", context.getSagaId());
            return;
        }

        log.info("Compensating CreatePaymentStep: deleting paymentId={}", context.getPaymentId());

        try {
            paymentRepository.deleteById(context.getPaymentId());
            log.info("Payment deleted successfully: paymentId={}", context.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to delete payment during compensation: paymentId={}",
                    context.getPaymentId(), e);
            // In a real system, this would be logged for manual reconciliation
        }
    }

    @Override
    public String getStepName() {
        return "CreatePayment";
    }
}
