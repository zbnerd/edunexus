package com.edunexusgraphql.controller;

import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.PlanSubscription;
import com.edunexusgraphql.service.dummy.DummyEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EnrollmentController {

    private final DummyEnrollmentService enrollmentService;

    @QueryMapping
    public Boolean checkCourseAccess(
            @Argument Long userId,
            @Argument Long courseId
    ) {
        return enrollmentService.checkCourseAccess(userId, courseId);
    }

    @QueryMapping
    public List<PlanSubscription> getUserPlanSubscriptions(
            @Argument Long userId
    ) {
        return enrollmentService.getSubscriptionsByUserId(userId);
    }

    @QueryMapping
    public List<Enrollment> getUserEnrollments(
            @Argument Long userId
    ) {
        return enrollmentService.getEnrollmentsByUserId(userId);
    }

    @MutationMapping
    public Payment purchaseCourse(
            @Argument Long userId,
            @Argument Long courseId,
            @Argument Float amount,
            @Argument String paymentMethod
    ) {
        return enrollmentService.purchaseCourse(userId, courseId, amount, paymentMethod);
    }
    @MutationMapping
    public Payment purchaseSubscription(
            @Argument Long userId,
            @Argument Float amount,
            @Argument String paymentMethod
    ) {
        return enrollmentService.purchaseSubscription(userId, amount, paymentMethod);
    }
}
