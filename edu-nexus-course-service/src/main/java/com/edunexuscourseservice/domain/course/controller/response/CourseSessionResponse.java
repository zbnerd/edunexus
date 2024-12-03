package com.edunexuscourseservice.domain.course.controller.response;

import com.edunexuscourseservice.domain.course.entity.CourseSession;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseSessionResponse {
    private Long id;
    private String title;

    public static CourseSessionResponse from(CourseSession courseSession) {
        return CourseSessionResponse.builder()
                .id(courseSession.getId())
                .title(courseSession.getTitle())
                .build();
    }
}
