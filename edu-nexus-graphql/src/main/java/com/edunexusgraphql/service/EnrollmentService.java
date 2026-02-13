package com.edunexusgraphql.service;

import com.edunexusenrollmentservice.domain.service.EnrollmentServiceGrpc;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import com.edunexusenrollmentservice.domain.service.FakePaymentServiceGrpc;
import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.PlanSubscription;
import com.edunexusgraphql.saga.PaymentOrchestrationService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    @GrpcClient("edu-nexus-enrollment-service")
    private EnrollmentServiceGrpc.EnrollmentServiceBlockingStub enrollmentStub;

    @GrpcClient("edu-nexus-payment-service")
    private FakePaymentServiceGrpc.FakePaymentServiceBlockingStub paymentStub;

    private final PaymentOrchestrationService paymentOrchestrationService;

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
        EnrollmentServiceOuterClass.CourseAccessRequest request = EnrollmentServiceOuterClass.CourseAccessRequest.newBuilder()
                .setCourseId(courseId)
                .setUserId(userId)
                .build();
        EnrollmentServiceOuterClass.CourseAccessResponse response = enrollmentStub.checkCourseAccess(request);
        return response.getHasAccess();
    }

    public boolean checkSubscriptionAccess(long userId) {
        EnrollmentServiceOuterClass.SubscriptionAccessRequest request = EnrollmentServiceOuterClass.SubscriptionAccessRequest.newBuilder()
                .setUserId(userId)
                .build();
        EnrollmentServiceOuterClass.SubscriptionAccessResponse response = enrollmentStub.checkSubscriptionAccess(request);
        return response.getHasAccess();
    }

    public List<Enrollment> getEnrollmentsByUserId(Long userId) {
        EnrollmentServiceOuterClass.UserEnrollmentsRequest request = EnrollmentServiceOuterClass.UserEnrollmentsRequest.newBuilder()
                .setUserId(userId)
                .build();
        EnrollmentServiceOuterClass.UserEnrollmentsResponse response = enrollmentStub.getUserEnrollments(request);
        return response.getEnrollmentsList().stream()
                .map(Enrollment::fromProto)
                .collect(Collectors.toList());
    }


    public List<PlanSubscription> getSubscriptionsByUserId(Long userId) {
        EnrollmentServiceOuterClass.UserSubscriptionsRequest request = EnrollmentServiceOuterClass.UserSubscriptionsRequest.newBuilder()
                .setUserId(userId)
                .build();
        EnrollmentServiceOuterClass.UserSubscriptionsResponse response = enrollmentStub.getUserPlanSubscriptions(request);
        return response.getSubscriptionsList().stream()
                .map(PlanSubscription::fromProto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "payment", key = "#paymentId")
    public Payment findPaymentById(Long paymentId) {
        EnrollmentServiceOuterClass.PaymentsByIdRequest request = EnrollmentServiceOuterClass.PaymentsByIdRequest.newBuilder()
                .setPaymentId(paymentId)
                .build();

        EnrollmentServiceOuterClass.PaymentsByIdResponse response = paymentStub.getPaymentsByPaymentId(request);
        return Payment.fromProto(response.getPayment());
    }

    public java.util.Map<Long, Payment> findPaymentsByIds(List<Long> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        return paymentIds.stream()
                .collect(java.util.stream.Collectors.toMap(
                        id -> id,
                        id -> findPaymentById(id)
                ));
    }
}
