package com.edunexuscourseservice.adapter.in.web;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
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
