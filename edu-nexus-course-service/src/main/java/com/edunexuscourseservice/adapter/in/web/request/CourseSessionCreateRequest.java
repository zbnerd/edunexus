package com.edunexuscourseservice.adapter.in.web.request;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseSessionCreateRequest {
    @jakarta.validation.constraints.NotBlank(message = "Title is required")
    @jakarta.validation.constraints.Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    public CourseSession toEntity() {
        CourseSession courseSession = new CourseSession();
        courseSession.setCourseSessionInfo(this.title);
        return courseSession;
    }
}
