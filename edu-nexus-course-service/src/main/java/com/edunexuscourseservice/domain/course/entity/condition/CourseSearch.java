package com.edunexuscourseservice.domain.course.entity.condition;

import lombok.Getter;

@Getter
public class CourseSearch {
    private String courseTitle;

    public CourseSearch() {
    }

    public CourseSearch(String courseTitle) {
        this.courseTitle = courseTitle;
    }

}
