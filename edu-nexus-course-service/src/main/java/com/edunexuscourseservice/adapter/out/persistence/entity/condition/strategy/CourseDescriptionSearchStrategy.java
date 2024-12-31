package com.edunexuscourseservice.adapter.out.persistence.entity.condition.strategy;

import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.edunexuscourseservice.adapter.out.persistence.entity.QCourse.course;

@Component
public class CourseDescriptionSearchStrategy implements CourseSearchStrategy {
    @Override
    public BooleanExpression apply(CourseSearchCondition condition) {
        if (StringUtils.hasText(condition.getDescription())) return course.description.containsIgnoreCase(condition.getDescription());
        return null;
    }
}
