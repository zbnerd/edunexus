package com.edunexusgraphql.config;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.resolver.EnrollmentDataResolver;
import com.edunexusgraphql.service.CourseService;
import com.edunexusgraphql.service.EnrollmentService;
import com.edunexusgraphql.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataLoaderConfig {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final BatchLoaderRegistry batchLoaderRegistry;

    public DataLoaderConfig(UserService userService, CourseService courseService, EnrollmentService enrollmentService, BatchLoaderRegistry batchLoaderRegistry) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.batchLoaderRegistry = batchLoaderRegistry;
    }

    @PostConstruct
    public void registerBatchLoaders() {
        // Register course batch loader
        batchLoaderRegistry.forTypePair(Long.class, Course.class)
                .registerMappedBatchLoader((courseIds, env) -> {
                    List<Long> ids = courseIds.stream().toList();
                    return Mono.justOrEmpty(courseService.findCoursesByIds(ids)
                            .stream()
                            .collect(Collectors.toMap(Course::getId, course -> course))
                    );
                });

        // Register user batch loader
        batchLoaderRegistry.forTypePair(Long.class, User.class)
                .registerMappedBatchLoader((userIds, env) -> {
                    List<Long> ids = userIds.stream().toList();
                    Map<Long, User> usersMap = userService.findUsersByIds(ids);
                    return Mono.justOrEmpty(usersMap);
                });

        // Register payment batch loader
        batchLoaderRegistry.forTypePair(Long.class, Payment.class)
                .registerMappedBatchLoader((paymentIds, env) -> {
                    List<Long> ids = paymentIds.stream().toList();
                    Map<Long, Payment> paymentsMap = enrollmentService.findPaymentsByIds(ids);
                    return Mono.justOrEmpty(paymentsMap);
                });
    }
}
