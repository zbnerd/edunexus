package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.condition.CourseSearch;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseRepositoryCustom {
    List<Course> findAll(CourseSearch courseSearch, Pageable pageable);
}
