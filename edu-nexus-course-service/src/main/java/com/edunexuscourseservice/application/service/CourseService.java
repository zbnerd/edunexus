package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.adapter.out.persistence.entity.redis.RCourse;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRedisRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexuscourseservice.port.in.CourseUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService implements CourseUseCase {

    private final CourseRepository courseRepository;
    private final CourseRedisRepository courseRedisRepository;

    @Transactional
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, Course newCourse) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        Course course = courseOptional.orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        Optional<RCourse> rCourseOptional = courseRedisRepository.findById(courseId);
        if (rCourseOptional.isPresent()) {
            courseRedisRepository.deleteById(courseId);
        }

        course.updateCourse(newCourse);

        return course;
    }

    public Optional<Course> getCourseById(Long courseId) {

        Optional<Course> cachedCourse = getCachedCourse(courseId);
        if (cachedCourse.isPresent()) {
            return cachedCourse;
        }

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        Course course = courseOptional.orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseRedisRepository.save(new RCourse(course));
        return courseOptional;
    }

    private Optional<Course> getCachedCourse(Long courseId) {
        Optional<RCourse> rCourseOptional = courseRedisRepository.findById(courseId);

        if(rCourseOptional.isPresent()) {
            RCourse rCourse = rCourseOptional.get();
            Course course = new Course();
            course.setCourseInfo(
                    CourseInfoDto.builder()
                            .title(rCourse.getTitle())
                            .description(rCourse.getDescription())
                            .instructorId(rCourse.getInstructorId())
                            .build()
            );

            return Optional.of(course);
        }

        return Optional.empty();

    }

    public List<Course> getAllCourses(CourseSearchCondition condition, Pageable pageable) {
        return courseRepository.findAll(condition, pageable);
    }

}
