package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.dummy.DummyCourseService;
import com.edunexusgraphql.service.dummy.DummyEnrollmentService;
import com.edunexusgraphql.service.dummy.DummyUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EnrollmentDataResolver {
    private final DummyUserService userService;
    private final DummyCourseService courseService;
    private final DummyEnrollmentService enrollmentService;

    @SchemaMapping(typeName = "Enrollment", field = "user")
    public User getUser(Enrollment enrollment) {
        return userService.findById(enrollment.getUserId()).orElse(null);
    }

    @SchemaMapping(typeName = "Enrollment", field = "course")
    public Course getCourse(Enrollment enrollment) {
        return courseService.findCourseById(enrollment.getCourseId()).orElse(null);
    }
//
//    @SchemaMapping(typeName = "Enrollment", field = "course")
//    public CompletableFuture<Course> getCourse(Enrollment enrollment, DataLoader<Long, Course> loader) {
//        return loader.load(enrollment.getCourseId());
//    }

    @SchemaMapping(typeName = "Enrollment", field = "payment")
    public Payment getPayment(Enrollment enrollment) {
        return enrollmentService.findPaymentById(enrollment.getPaymentId());
    }
}
