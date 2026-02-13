package com.edunexusgraphql.service;

import com.edunexusenrollmentservice.domain.service.EnrollmentServiceGrpc;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import com.edunexusenrollmentservice.domain.service.FakePaymentServiceGrpc;
import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.PlanSubscription;
import com.edunexusgraphql.port.client.EnrollmentClient;
import com.edunexusgraphql.saga.PaymentOrchestrationService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for enrollment-related operations.
 * Delegates client operations to EnrollmentClient interface.
 * Private gRPC methods retained for saga orchestrator use.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentClient enrollmentClient;
    private final PaymentOrchestrationService paymentOrchestrationService;

    // Private gRPC stubs for saga orchestrator use only
    @GrpcClient("edu-nexus-enrollment-service")
    private EnrollmentServiceGrpc.EnrollmentServiceBlockingStub enrollmentStub;

    @GrpcClient("edu-nexus-payment-service")
    private FakePaymentServiceGrpc.FakePaymentServiceBlockingStub paymentStub;

    public Payment purchaseCourse(Long userId, Long courseId, Double amount, String paymentMethod) {
        // Use Saga orchestrator for distributed transaction
        return paymentOrchestrationService.purchaseCourseWithSaga(
                userId, courseId, amount, paymentMethod, "COURSE"
        );
    }

    public Payment purchaseSubscription(long userId, double amount, String paymentMethod) {
        // Use Saga orchestrator for distributed transaction
        return paymentOrchestrationService.purchaseCourseWithSaga(
                userId, null, amount, paymentMethod, "SUBSCRIPTION"
        );
    }

    // Private methods retained for saga orchestrator use
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
        EnrollmentServiceOuterClass.CourseRegistrationRequest request = EnrollmentServiceOuterClass.CourseRegistrationRequest.newBuilder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setPaymentId(paymentId)
                .build();
        return enrollmentStub.registerCourse(request);
    }

    private EnrollmentServiceOuterClass.SubscriptionResponse manageSubscription(long userId, long startDate, long endDate, long paymentId) {
        EnrollmentServiceOuterClass.SubscriptionRequest request = EnrollmentServiceOuterClass.SubscriptionRequest.newBuilder()
                .setUserId(userId)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setPaymentId(paymentId)
                .build();
        return enrollmentStub.manageSubscription(request);
    }

    public boolean checkCourseAccess(long courseId, long userId) {
        return enrollmentClient.checkCourseAccess(courseId, userId);
    }

    public boolean checkSubscriptionAccess(long userId) {
        return enrollmentClient.checkSubscriptionAccess(userId);
    }

    public List<Enrollment> getEnrollmentsByUserId(Long userId) {
        return enrollmentClient.getEnrollmentsByUserId(userId);
    }

    public List<PlanSubscription> getSubscriptionsByUserId(Long userId) {
        return enrollmentClient.getSubscriptionsByUserId(userId);
    }

    public Payment findPaymentById(Long paymentId) {
        return enrollmentClient.findPaymentById(paymentId);
    }

    public java.util.Map<Long, Payment> findPaymentsByIds(List<Long> paymentIds) {
        return enrollmentClient.findPaymentsByIds(paymentIds);
    }
}
