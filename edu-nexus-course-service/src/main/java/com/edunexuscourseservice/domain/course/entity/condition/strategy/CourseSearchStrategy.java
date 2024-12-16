package com.edunexuscourseservice.domain.course.entity.condition.strategy;


import com.edunexuscourseservice.domain.course.entity.condition.CourseSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface CourseSearchStrategy {
    BooleanExpression apply(CourseSearchCondition condition);
}
