package com.edunexuscourseservice.domain.course.entity.condition;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CourseSearchCondition {
    private final String title;
    private final String description;
}
