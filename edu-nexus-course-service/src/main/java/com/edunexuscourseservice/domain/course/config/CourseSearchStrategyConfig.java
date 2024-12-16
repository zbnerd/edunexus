package com.edunexuscourseservice.domain.course.config;

import com.edunexuscourseservice.domain.course.entity.condition.context.CourseSearchConditionContext;
import com.edunexuscourseservice.domain.course.entity.condition.strategy.CourseDescriptionSearchStrategy;
import com.edunexuscourseservice.domain.course.entity.condition.strategy.CourseTitleSearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseSearchStrategyConfig {

    @Bean
    public CourseSearchConditionContext courseSearchConditionContext() {
        CourseSearchConditionContext context = new CourseSearchConditionContext();
        context.addStrategy(new CourseTitleSearchStrategy());
        context.addStrategy(new CourseDescriptionSearchStrategy());
        return context;
    }

}
