package com.edunexuscourseservice.domain.course.entity.condition.strategy;

import com.edunexuscourseservice.domain.course.entity.condition.CourseSearch;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface SearchStrategy {
    BooleanExpression getSearchCondition(CourseSearch search);
}
