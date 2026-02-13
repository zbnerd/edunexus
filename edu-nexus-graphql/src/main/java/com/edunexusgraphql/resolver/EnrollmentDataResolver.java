package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.CourseService;
import com.edunexusgraphql.service.EnrollmentService;
import com.edunexusgraphql.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
public class EnrollmentDataResolver {
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public EnrollmentDataResolver(UserService userService, CourseService courseService, EnrollmentService enrollmentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @SchemaMapping(typeName = "Enrollment", field = "user")
    public CompletableFuture<User> getUser(Enrollment enrollment, DataLoader<Long, User> userDataLoader) {
        return userDataLoader.load(enrollment.getUserId());
    }

    @SchemaMapping(typeName = "Enrollment", field = "course")
    public CompletableFuture<Course> getCourse(Enrollment enrollment, DataLoader<Long, Course> courseDataLoader) {
        return courseDataLoader.load(enrollment.getCourseId());
    }

    @SchemaMapping(typeName = "Enrollment", field = "payment")
    public CompletableFuture<Payment> getPayment(Enrollment enrollment, DataLoader<Long, Payment> paymentDataLoader) {
        return paymentDataLoader.load(enrollment.getPaymentId());
    }
}
