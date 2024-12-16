package com.edunexuscourseservice.domain.course.controller;

import com.edunexuscourseservice.domain.course.controller.response.CourseInfoResponse;
import com.edunexuscourseservice.domain.course.controller.response.CourseResponse;
import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.service.CourseRatingService;
import com.edunexuscourseservice.domain.course.service.CourseService;
import com.edunexuscourseservice.domain.course.util.RoundUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseRatingService courseRatingService;

    // 강의 생성
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseCreateRequest request) {
        CourseInfoDto courseInfoDto = request.toCourseInfoDto();
        Course course = new Course();
        course.setCourseInfo(courseInfoDto);

        Course savedCourse = courseService.saveCourse(course);
        CourseResponse response = CourseResponse.from(savedCourse);

        return ResponseEntity.created(URI.create("/courses/" + savedCourse.getId())).body(response);
    }

    // 강의 정보 업데이트
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId,
                                                       @RequestBody CourseUpdateRequest request) {
        CourseInfoDto courseInfoDto = request.toCourseInfoDto();
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        Course newCourse = new Course();
        newCourse.setCourseInfo(courseInfoDto);
        Course updatedCourse = courseService.updateCourse(course.getId(), newCourse);

        return ResponseEntity.ok(CourseResponse.from(updatedCourse));
    }

    // 특정 강의 정보 조회
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseInfoResponse> getCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        Double courseRatingAvg = courseRatingService.getAverageRatingByCourseId(courseId);

        return ResponseEntity.ok(CourseInfoResponse.from(course, RoundUtils.roundToNDecimals(courseRatingAvg, 2)));
    }

    // 모든 강의 목록 조회
    @GetMapping
    public ResponseEntity<List<CourseInfoResponse>> getAllCoursesByTitle(
            @RequestParam String courseTitle,
            @RequestParam String courseDescription,
            Pageable pageable
    ) {
        CourseSearchCondition condition = new CourseSearchCondition(courseTitle, courseDescription);
        List<Course> courses = courseService.getAllCourses(condition, pageable);
        List<CourseInfoResponse> responses = courses.stream()
                .map(course -> CourseInfoResponse.from(course,
                        RoundUtils.roundToNDecimals(courseRatingService.getAverageRatingByCourseId(course.getId()), 2)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Getter
    static class CourseCreateRequest {
        private String title;
        private String description;
        private Long instructorId;

        public CourseInfoDto toCourseInfoDto() {
            return CourseInfoDto.builder()
                    .title(this.title)
                    .description(this.description)
                    .instructorId(this.instructorId)
                    .build();
        }
    }

    @Getter
    static class CourseUpdateRequest {
        private String title;
        private String description;
        private Long instructorId;

        public CourseInfoDto toCourseInfoDto() {
            return CourseInfoDto.builder()
                    .title(this.title)
                    .description(this.description)
                    .instructorId(this.instructorId)
                    .build();
        }
    }
}