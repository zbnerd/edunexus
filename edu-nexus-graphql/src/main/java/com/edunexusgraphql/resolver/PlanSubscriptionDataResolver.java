package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.PlanSubscription;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.EnrollmentService;
import com.edunexusgraphql.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PlanSubscriptionDataResolver {

    private final UserService userService;
    private final EnrollmentService enrollmentService;

    @SchemaMapping(typeName = "PlanSubscription", field = "user")
    public User getUser(PlanSubscription subscription) {
        return userService.findById(subscription.getUserId()).orElse(null);
    }

    @SchemaMapping(typeName = "PlanSubscription", field = "payment")
    public Payment getPayment(PlanSubscription subscription) {
        return enrollmentService.findPaymentById(subscription.getPaymentId());
    }

}
