package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PaymentDataResolver {

    private final UserService userService;


    @SchemaMapping(typeName = "Payment", field = "user")
    public User getUser(Payment payment) {
        return userService.findById(payment.getUserId()).orElse(null);
    }
}
