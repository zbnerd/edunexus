package com.edunexuscourseservice.domain.course.entity.condition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseSearchCondition {
    private String title;
    private String description;
}
