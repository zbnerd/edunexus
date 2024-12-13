package com.edunexuscourseservice.domain.course.controller.response;

import com.edunexuscourseservice.domain.course.entity.Course;
import lombok.Getter;

@Getter
public class CourseInfoResponse {
    private Long id;
    private String title;
    private String description;
    private Long instructorId;
    private Double courseRatingAvg;

    public static CourseInfoResponse from(Course course, Double courseRatingAvg) {
        CourseInfoResponse response = new CourseInfoResponse();
        response.id = course.getId();
        response.title = course.getTitle();
        response.description = course.getDescription();
        response.instructorId = course.getInstructorId();
        response.courseRatingAvg = courseRatingAvg;
        return response;
    }
}