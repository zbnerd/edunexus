package com.edunexusenrollmentservice.domain.grpc;

import com.edunexusenrollmentservice.domain.dto.EnrollmentDto;
import com.edunexusenrollmentservice.domain.dto.SubscriptionDto;
import com.edunexusenrollmentservice.domain.entity.Enrollment;
import com.edunexusenrollmentservice.domain.entity.Subscription;
import com.edunexusenrollmentservice.domain.service.EnrollmentService;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceGrpc;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import handler.GrpcResponseHandler;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EnrollmentGrpcService extends EnrollmentServiceGrpc.EnrollmentServiceImplBase {

    private final EnrollmentService enrollmentService;

    @Override
    public void registerCourse(
            EnrollmentServiceOuterClass.CourseRegistrationRequest request,
            StreamObserver<EnrollmentServiceOuterClass.CourseRegistrationResponse> responseObserver
    ) {

        Enrollment enrollment = enrollmentService.registerCourse(
                EnrollmentDto.builder()
                        .userId(request.getUserId())
                        .courseId(request.getCourseId())
                        .paymentId(request.getPaymentId())
                        .build()
        );

        EnrollmentServiceOuterClass.CourseRegistrationResponse response =
                EnrollmentServiceOuterClass
                        .CourseRegistrationResponse
                        .newBuilder()
                        .setUserId(enrollment.getUserId())
                        .setCourseId(enrollment.getCourseId())
                        .build();

        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void manageSubscription(
            EnrollmentServiceOuterClass.SubscriptionRequest request,
            StreamObserver<EnrollmentServiceOuterClass.SubscriptionResponse> responseObserver
    ) {

        LocalDateTime startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getStartDate()), ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getEndDate()), ZoneId.systemDefault());

        Subscription subscription = enrollmentService.manageSubscription(
                SubscriptionDto.builder()
                        .userId(request.getUserId())
                        .startDate(startDate)
                        .endDate(endDate)
                        .paymentId(request.getPaymentId())
                        .build()
        );

        EnrollmentServiceOuterClass.SubscriptionResponse response =
                EnrollmentServiceOuterClass.SubscriptionResponse
                        .newBuilder()
                        .setSubscription(subscription.toProto())
                        .build();

        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void renewSubscription(
            EnrollmentServiceOuterClass.RenewSubscriptionRequest request,
            StreamObserver<EnrollmentServiceOuterClass.SubscriptionResponse> responseObserver
    ) {
        LocalDateTime startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getStartDate()), ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getEndDate()), ZoneId.systemDefault());


        Subscription subscription = enrollmentService.renewSubscription(
                request.getSubscriptionId(),
                startDate,
                endDate
        );

        EnrollmentServiceOuterClass.SubscriptionResponse response =
                EnrollmentServiceOuterClass.SubscriptionResponse
                        .newBuilder()
                        .setSubscription(subscription.toProto())
                        .build();

        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void checkCourseAccess(EnrollmentServiceOuterClass.CourseAccessRequest request,
                                  StreamObserver<EnrollmentServiceOuterClass.CourseAccessResponse> responseObserver) {
        boolean hasAccess = enrollmentService.checkCourseAccess(request.getUserId(), request.getCourseId());
        EnrollmentServiceOuterClass.CourseAccessResponse response =
                EnrollmentServiceOuterClass.CourseAccessResponse
                        .newBuilder()
                        .setHasAccess(hasAccess)
                        .build();

        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void checkSubscriptionAccess(EnrollmentServiceOuterClass.SubscriptionAccessRequest request,
                                        StreamObserver<EnrollmentServiceOuterClass.SubscriptionAccessResponse> responseObserver) {
        boolean hasAccess = enrollmentService.checkSubscriptionAccess(request.getUserId(), LocalDateTime.now());
        EnrollmentServiceOuterClass.SubscriptionAccessResponse response =
                EnrollmentServiceOuterClass.SubscriptionAccessResponse
                        .newBuilder()
                        .setHasAccess(hasAccess)
                        .build();

        GrpcResponseHandler.sendResponse(response, responseObserver);
    }

    @Override
    public void getUserEnrollments(EnrollmentServiceOuterClass.UserEnrollmentsRequest request,
                                   StreamObserver<EnrollmentServiceOuterClass.UserEnrollmentsResponse> responseObserver) {
        List<Enrollment> enrollments = enrollmentService.getUserEnrollments(request.getUserId());
        EnrollmentServiceOuterClass.UserEnrollmentsResponse.Builder responseBuilder =
                EnrollmentServiceOuterClass.UserEnrollmentsResponse.newBuilder();

        for (Enrollment enrollment : enrollments) {
            responseBuilder.addEnrollments(enrollment.toProto());
        }

        GrpcResponseHandler.sendResponse(responseBuilder.build(), responseObserver);
    }

    @Override
    public void getUserPlanSubscriptions(EnrollmentServiceOuterClass.UserSubscriptionsRequest request,
                                         StreamObserver<EnrollmentServiceOuterClass.UserSubscriptionsResponse> responseObserver) {
        List<Subscription> subscriptions = enrollmentService.getUserPlanSubscriptions(request.getUserId());
        EnrollmentServiceOuterClass.UserSubscriptionsResponse.Builder responseBuilder =
                EnrollmentServiceOuterClass.UserSubscriptionsResponse.newBuilder();

        for (Subscription subscription : subscriptions) {
            responseBuilder.addSubscriptions(subscription.toProto());
        }

        GrpcResponseHandler.sendResponse(responseBuilder.build(), responseObserver);
    }
}
