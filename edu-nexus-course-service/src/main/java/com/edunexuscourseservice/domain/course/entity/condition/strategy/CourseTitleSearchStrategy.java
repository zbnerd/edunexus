package com.edunexuscourseservice.domain.course.entity.condition.strategy;

import com.edunexuscourseservice.domain.course.entity.condition.CourseSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.StringUtils;

import static com.edunexuscourseservice.domain.course.entity.QCourse.course;

public class CourseTitleSearchStrategy implements CourseSearchStrategy {
    @Override
    public BooleanExpression apply(CourseSearchCondition condition) {
        if (StringUtils.hasText(condition.getTitle())) return course.title.containsIgnoreCase(condition.getTitle());
        return null;
    }
}
