package com.edunexuscourseservice.port.in;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CourseUseCase {
    Course saveCourse(Course course);
    Course updateCourse(Long courseId, Course newCourse);
    Optional<Course> getCourseById(Long courseId);
    List<Course> getAllCourses(CourseSearchCondition condition, Pageable pageable);

    /**
     * Batch fetch courses by IDs to avoid N+1 queries.
     * Optimized for GraphQL batch loading and other scenarios requiring multiple courses.
     *
     * @param courseIds List of course IDs
     * @return List of courses (only found ones)
     */
    List<Course> getCoursesByIds(List<Long> courseIds);
}
