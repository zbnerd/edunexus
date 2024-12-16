package com.edunexuscourseservice.domain.course.entity.condition.context;

import com.edunexuscourseservice.domain.course.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.domain.course.entity.condition.strategy.CourseSearchStrategy;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.List;
import java.util.Objects;

public class CourseSearchConditionContext {
    private final List<CourseSearchStrategy> strategies;

    public CourseSearchConditionContext(List<CourseSearchStrategy> strategies) {
        this.strategies = strategies;
    }

    public BooleanExpression buildExpression(CourseSearchCondition condition) {
        return strategies.stream()
                .map(strategy -> strategy.apply(condition))
                .filter(Objects::nonNull)
                .reduce(BooleanExpression::or)
                .orElse(null);
    }
}
