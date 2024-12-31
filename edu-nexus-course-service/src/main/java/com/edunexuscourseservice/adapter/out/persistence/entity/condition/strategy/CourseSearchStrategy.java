package com.edunexuscourseservice.adapter.out.persistence.entity.condition.strategy;


import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface CourseSearchStrategy {
    BooleanExpression apply(CourseSearchCondition condition);
}
