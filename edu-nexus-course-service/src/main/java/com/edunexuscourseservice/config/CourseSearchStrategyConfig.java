package com.edunexuscourseservice.config;

import com.edunexuscourseservice.adapter.out.persistence.entity.condition.context.CourseSearchConditionContext;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.strategy.CourseSearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CourseSearchStrategyConfig {

    private final List<CourseSearchStrategy> strategies;

    public CourseSearchStrategyConfig(List<CourseSearchStrategy> strategies) {
        this.strategies = strategies;
    }

    @Bean
    public CourseSearchConditionContext courseSearchConditionContext() {
        return new CourseSearchConditionContext(strategies);
    }

}
