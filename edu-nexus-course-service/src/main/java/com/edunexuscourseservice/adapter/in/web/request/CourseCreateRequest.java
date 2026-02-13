package com.edunexuscourseservice.adapter.in.web.request;

import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import lombok.Getter;

@Getter
public class CourseCreateRequest {
    @jakarta.validation.constraints.NotBlank(message = "Title is required")
    @jakarta.validation.constraints.Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @jakarta.validation.constraints.Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @jakarta.validation.constraints.NotNull(message = "Instructor ID is required")
    private Long instructorId;

    public CourseInfoDto toCourseInfoDto() {
        return CourseInfoDto.builder()
                .title(this.title)
                .description(this.description)
                .instructorId(this.instructorId)
                .build();
    }
}
