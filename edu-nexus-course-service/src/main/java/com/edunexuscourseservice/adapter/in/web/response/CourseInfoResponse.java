package com.edunexuscourseservice.adapter.in.web.response;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CourseInfoResponse {

    @Setter
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