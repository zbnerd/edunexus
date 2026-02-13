package com.edunexusgraphql.config;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.resolver.EnrollmentDataResolver;
import com.edunexusgraphql.service.CourseService;
import com.edunexusgraphql.service.EnrollmentService;
import com.edunexusgraphql.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DataLoaderConfig {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public DataLoaderConfig(UserService userService, CourseService courseService, EnrollmentService enrollmentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry(BatchLoaderRegistry registry, UserService userService, CourseService courseService, EnrollmentService enrollmentService) {
        // Register course batch loader
        registry.forTypePair(Long.class, Course.class)
                .registerMappedBatchLoader((courseIds, env) -> {
                    List<Long> ids = courseIds.stream().toList();
                    return Mono.justOrEmpty(courseService.findCoursesByIds(ids)
                            .stream()
                            .collect(Collectors.toMap(Course::getId, course -> course))
                    );
                });

        // Register user batch loader
        registry.forTypePair(Long.class, User.class)
                .registerMappedBatchLoader((userIds, env) -> {
                    List<Long> ids = userIds.stream().toList();
                    Map<Long, User> usersMap = userService.findUsersByIds(ids);
                    return Mono.justOrEmpty(usersMap);
                });

        // Register payment batch loader
        registry.forTypePair(Long.class, Payment.class)
                .registerMappedBatchLoader((paymentIds, env) -> {
                    List<Long> ids = paymentIds.stream().toList();
                    Map<Long, Payment> paymentsMap = enrollmentService.findPaymentsByIds(ids);
                    return Mono.justOrEmpty(paymentsMap);
                });

        return registry;
    }
}
