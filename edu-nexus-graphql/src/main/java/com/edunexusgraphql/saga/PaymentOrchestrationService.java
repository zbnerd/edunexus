package com.edunexusgraphql.saga;

import com.edunexusenrollmentservice.domain.service.EnrollmentServiceGrpc;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import com.edunexusenrollmentservice.domain.service.FakePaymentServiceGrpc;
import com.edunexusgraphql.constants.SubscriptionConstants;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.service.EnrollmentService;
import com.edunexusgraphql.service.kafka.PaymentProducerService;
import com.edunexusgraphql.saga.event.EnrollmentResultEvent;
import com.edunexusgraphql.saga.event.PaymentConfirmedEvent;
import com.edunexusgraphql.saga.event.PaymentCreatedEvent;
import com.edunexusgraphql.saga.event.PaymentFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Saga Orchestrator for Payment and Enrollment Distributed Transaction
 *
 * Flow:
 * 1. Create payment with PENDING status
 * 2. Publish PaymentCreatedEvent to Kafka
 * 3. Register enrollment via gRPC
 * 4a. SUCCESS: Publish PaymentConfirmedEvent, update payment to CONFIRMED
 * 4b. FAILURE: Publish PaymentFailedEvent to trigger compensation
 *
 * Compensation on failure:
 * - Payment remains PENDING for manual review
 * - Future: automatic payment void/refund
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestrationService {

    @GrpcClient("edu-nexus-enrollment-service")
    private EnrollmentServiceGrpc.EnrollmentServiceBlockingStub enrollmentStub;

    @GrpcClient("edu-nexus-payment-service")
    private FakePaymentServiceGrpc.FakePaymentServiceBlockingStub paymentStub;

    private final PaymentProducerService paymentProducerService;
    private final ObjectMapper objectMapper;

    /**
     * Execute saga: Create payment → Register enrollment → Confirm or fail
     *
     * @param userId User ID
     * @param courseId Course ID (null for subscription)
     * @param amount Payment amount
     * @param paymentMethod Payment method (CARD, KAKAO, etc.)
     * @param type Payment type (COURSE or SUBSCRIPTION)
     * @return Payment object
     */
    public Payment purchaseCourseWithSaga(Long userId, Long courseId, Double amount, String paymentMethod, String type) {
        Payment payment = null;
        Long paymentId = null;

        try {
            // Step 1: Create PENDING payment
            log.info("Step 1: Creating PENDING payment for userId={}, courseId={}", userId, courseId);
            payment = createPayment(userId, type, amount, paymentMethod);
            paymentId = payment.getId();

            // Step 2: Publish PaymentCreatedEvent
            log.info("Step 2: Publishing PaymentCreatedEvent for paymentId={}", paymentId);
            PaymentCreatedEvent createdEvent = PaymentCreatedEvent.create(
                    paymentId, userId, type, amount, paymentMethod, courseId);
            paymentProducerService.sendPaymentCreatedEvent(createdEvent);

            // Step 3: Register enrollment (or subscription)
            log.info("Step 3: Registering enrollment for paymentId={}", paymentId);
            if ("COURSE".equals(type)) {
                EnrollmentServiceOuterClass.CourseRegistrationResponse response =
                        registerCourse(userId, courseId, paymentId);
                log.info("Enrollment registered successfully: courseId={}, userId={}", response.getCourseId(), response.getUserId());

                // Step 4a: SUCCESS - Confirm payment
                confirmPayment(paymentId, userId, courseId);
                payment.setStatus("CONFIRMED");

                // Publish confirmation event
                PaymentConfirmedEvent confirmedEvent = PaymentConfirmedEvent.create(paymentId, userId, courseId);
                paymentProducerService.sendPaymentConfirmedEvent(confirmedEvent);

            } else if ("SUBSCRIPTION".equals(type)) {
                manageSubscription(userId, System.currentTimeMillis(),
                        System.currentTimeMillis() + SubscriptionConstants.DEFAULT_SUBSCRIPTION_DURATION_MS, paymentId);
                log.info("Subscription managed successfully for paymentId={}", paymentId);

                // Step 4a: SUCCESS - Confirm payment
                confirmPayment(paymentId, userId, null);
                payment.setStatus("CONFIRMED");

                // Publish confirmation event
                PaymentConfirmedEvent confirmedEvent = PaymentConfirmedEvent.create(paymentId, userId, null);
                paymentProducerService.sendPaymentConfirmedEvent(confirmedEvent);
            }

            return payment;

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed for paymentId={}: {}", paymentId, e.getStatus());

            // Step 4b: FAILURE - Publish PaymentFailedEvent
            if (paymentId != null) {
                PaymentFailedEvent failedEvent = PaymentFailedEvent.create(
                        paymentId, userId, "Enrollment service unavailable: " + e.getStatus().getDescription());
                paymentProducerService.sendPaymentFailedEvent(failedEvent);
            }

            throw new RuntimeException("Failed to complete enrollment saga", e);

        } catch (Exception e) {
            log.error("Saga failed for paymentId={}: {}", paymentId, e.getMessage(), e);

            // Step 4b: FAILURE - Publish PaymentFailedEvent
            if (paymentId != null) {
                PaymentFailedEvent failedEvent = PaymentFailedEvent.create(
                        paymentId, userId, "Enrollment failed: " + e.getMessage());
                paymentProducerService.sendPaymentFailedEvent(failedEvent);
            }

            throw new RuntimeException("Failed to complete enrollment saga", e);
        }
    }

    /**
     * Handle enrollment result events from enrollment service (async pattern)
     * This listener is for future enhancement when enrollment service publishes async events
     */
    @KafkaListener(topics = "enrollment-result", groupId = "payment-saga-group")
    public void handleEnrollmentResult(String message) {
        try {
            EnrollmentResultEvent event = objectMapper.readValue(message, EnrollmentResultEvent.class);
            log.info("Received enrollment result: paymentId={}, status={}",
                    event.getPaymentId(), event.getStatus());

            if ("SUCCESS".equals(event.getStatus())) {
                // Payment already confirmed in synchronous flow
                log.debug("Enrollment success confirmed for paymentId={}", event.getPaymentId());
            } else if ("FAILED".equals(event.getStatus())) {
                // Compensation: Mark payment as failed
                log.warn("Enrollment failed for paymentId={}, reason={}",
                        event.getPaymentId(), event.getErrorMessage());

                PaymentFailedEvent failedEvent = PaymentFailedEvent.create(
                        event.getPaymentId(),
                        event.getUserId(),
                        event.getErrorMessage() != null ? event.getErrorMessage() : "Enrollment failed"
                );
                paymentProducerService.sendPaymentFailedEvent(failedEvent);
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse enrollment result message: {}", message, e);
            throw new RuntimeException("Failed to parse Kafka message", e);
        } catch (Exception e) {
            log.warn("Failed to process enrollment result (will retry): {}", message, e);
            throw e;
        }
    }

    private Payment createPayment(long userId, String type, double amount, String paymentMethod) {
        EnrollmentServiceOuterClass.PaymentRequest request =
                EnrollmentServiceOuterClass.PaymentRequest.newBuilder()
                        .setUserId(userId)
                        .setType(type)
                        .setAmount(amount)
                        .setPaymentMethod(paymentMethod)
                        .build();

        return Payment.fromProto(paymentStub.createPayment(request));
    }

    private EnrollmentServiceOuterClass.CourseRegistrationResponse registerCourse(long userId, long courseId, long paymentId) {
        EnrollmentServiceOuterClass.CourseRegistrationRequest request =
                EnrollmentServiceOuterClass.CourseRegistrationRequest.newBuilder()
                        .setUserId(userId)
                        .setCourseId(courseId)
                        .setPaymentId(paymentId)
                        .build();
        return enrollmentStub.registerCourse(request);
    }

    private EnrollmentServiceOuterClass.SubscriptionResponse manageSubscription(long userId, long startDate, long endDate, long paymentId) {
        EnrollmentServiceOuterClass.SubscriptionRequest request =
                EnrollmentServiceOuterClass.SubscriptionRequest.newBuilder()
                        .setUserId(userId)
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .setPaymentId(paymentId)
                        .build();
        return enrollmentStub.manageSubscription(request);
    }

    private void confirmPayment(long paymentId, long userId, Long courseId) {
        // TODO: Add gRPC call to payment service to update payment status to CONFIRMED
        // For now, status is updated locally in the Payment object
        log.info("Payment confirmed: paymentId={}, userId={}", paymentId, userId);
    }
}
