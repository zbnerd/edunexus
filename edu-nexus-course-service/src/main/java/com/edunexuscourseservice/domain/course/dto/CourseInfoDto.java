package com.edunexuscourseservice.domain.course.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseInfoDto {
    private String title;
    private String description;
    private Long instructorId;
}
