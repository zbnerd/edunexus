package com.edunexuscourseservice.adapter.in.web.response;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import lombok.Getter;

@Getter
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private Long instructorId;

    public static CourseResponse from(Course course) {
        CourseResponse response = new CourseResponse();
        response.id = course.getId();
        response.title = course.getTitle();
        response.description = course.getDescription();
        response.instructorId = course.getInstructorId();
        return response;
    }
}