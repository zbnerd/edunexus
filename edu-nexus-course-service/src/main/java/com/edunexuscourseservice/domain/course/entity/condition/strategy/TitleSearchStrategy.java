package com.edunexuscourseservice.domain.course.entity.condition.strategy;

import com.edunexuscourseservice.domain.course.entity.condition.CourseSearch;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.StringUtils;

import static com.edunexuscourseservice.domain.course.entity.QCourse.course;

public class TitleSearchStrategy implements SearchStrategy {
    @Override
    public BooleanExpression getSearchCondition(CourseSearch search) {
        if (StringUtils.hasText(search.getCourseTitle()))
            return null;
        return course.title.containsIgnoreCase(search.getCourseTitle());
    }
}
