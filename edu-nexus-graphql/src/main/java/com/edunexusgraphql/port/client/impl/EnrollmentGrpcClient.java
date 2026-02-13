package com.edunexusgraphql.port.client.impl;

import com.edunexusenrollmentservice.domain.service.EnrollmentServiceGrpc;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import com.edunexusenrollmentservice.domain.service.FakePaymentServiceGrpc;
import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.PlanSubscription;
import com.edunexusgraphql.port.client.EnrollmentClient;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * gRPC-based implementation of EnrollmentClient.
 * Wraps gRPC stubs and handles protocol buffer conversions.
 */
@Slf4j
@Component
public class EnrollmentGrpcClient implements EnrollmentClient {

    @GrpcClient("edu-nexus-enrollment-service")
    private EnrollmentServiceGrpc.EnrollmentServiceBlockingStub enrollmentStub;

    @GrpcClient("edu-nexus-payment-service")
    private FakePaymentServiceGrpc.FakePaymentServiceBlockingStub paymentStub;

    @Override
    public boolean checkCourseAccess(long courseId, long userId) {
        log.debug("Checking course access: userId={}, courseId={}", userId, courseId);
        EnrollmentServiceOuterClass.CourseAccessRequest request =
                EnrollmentServiceOuterClass.CourseAccessRequest.newBuilder()
                        .setCourseId(courseId)
                        .setUserId(userId)
                        .build();
        EnrollmentServiceOuterClass.CourseAccessResponse response = enrollmentStub.checkCourseAccess(request);
        return response.getHasAccess();
    }

    @Override
    public boolean checkSubscriptionAccess(long userId) {
        log.debug("Checking subscription access: userId={}", userId);
        EnrollmentServiceOuterClass.SubscriptionAccessRequest request =
                EnrollmentServiceOuterClass.SubscriptionAccessRequest.newBuilder()
                        .setUserId(userId)
                        .build();
        EnrollmentServiceOuterClass.SubscriptionAccessResponse response = enrollmentStub.checkSubscriptionAccess(request);
        return response.getHasAccess();
    }

    @Override
    public List<Enrollment> getEnrollmentsByUserId(Long userId) {
        log.debug("Getting enrollments for userId={}", userId);
        EnrollmentServiceOuterClass.UserEnrollmentsRequest request =
                EnrollmentServiceOuterClass.UserEnrollmentsRequest.newBuilder()
                        .setUserId(userId)
                        .build();
        EnrollmentServiceOuterClass.UserEnrollmentsResponse response = enrollmentStub.getUserEnrollments(request);
        return response.getEnrollmentsList().stream()
                .map(Enrollment::fromProto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanSubscription> getSubscriptionsByUserId(Long userId) {
        log.debug("Getting subscriptions for userId={}", userId);
        EnrollmentServiceOuterClass.UserSubscriptionsRequest request =
                EnrollmentServiceOuterClass.UserSubscriptionsRequest.newBuilder()
                        .setUserId(userId)
                        .build();
        EnrollmentServiceOuterClass.UserSubscriptionsResponse response = enrollmentStub.getUserPlanSubscriptions(request);
        return response.getSubscriptionsList().stream()
                .map(PlanSubscription::fromProto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "payment", key = "#paymentId")
    public Payment findPaymentById(Long paymentId) {
        log.debug("Finding payment by paymentId={}", paymentId);
        EnrollmentServiceOuterClass.PaymentsByIdRequest request =
                EnrollmentServiceOuterClass.PaymentsByIdRequest.newBuilder()
                        .setPaymentId(paymentId)
                        .build();

        EnrollmentServiceOuterClass.PaymentsByIdResponse response = paymentStub.getPaymentsByPaymentId(request);
        return Payment.fromProto(response.getPayment());
    }

    @Override
    public Map<Long, Payment> findPaymentsByIds(List<Long> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("Finding payments for {} paymentIds", paymentIds.size());
        return paymentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        this::findPaymentById
                ));
    }
}
