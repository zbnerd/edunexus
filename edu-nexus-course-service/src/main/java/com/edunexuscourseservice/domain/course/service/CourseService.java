package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, Course newCourse) {
        Course course = getCourseById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        course.updateCourse(newCourse);

        return course;
    }

    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    /* 페이징 적용 필요. */

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}
