package com.edunexuscourseservice.adapter.in.web;

import com.edunexuscourseservice.adapter.in.web.response.CourseInfoResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseRatingAverageResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseResponse;
import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.application.service.CourseService;
import com.edunexuscourseservice.domain.course.util.RoundUtils;
import com.edunexuscourseservice.port.in.CourseRatingUseCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseRatingUseCase courseRatingService;

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
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));
        CourseInfoDto courseInfoDto = request.toCourseInfoDto(course.getInstructorId());

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

    // 특정 강의 평균 평점 조회
    @GetMapping("/{courseId}/course-rating-average")
    public ResponseEntity<CourseRatingAverageResponse> getCourseRatingAverage(
            @PathVariable Long courseId
    ) {
        Double courseRatingAvg = courseRatingService.getAverageRatingByCourseId(courseId);

        return ResponseEntity.ok(CourseRatingAverageResponse.from(courseId, courseRatingAvg));
    }

    // 모든 강의 목록 조회
    @GetMapping
    public ResponseEntity<List<CourseInfoResponse>> getAllCourses(
            @ModelAttribute CourseSearchCondition condition,
            Pageable pageable
    ) {
        List<Course> courses = courseService.getAllCourses(condition, pageable);

        // Batch fetch all course ratings at once to avoid N+1 query
        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        // Use batch lookup for ratings (optimized to avoid N+1)
        Map<Long, Double> averageRatings = courseRatingService.getAverageRatingsByCourseIds(courseIds);

        List<CourseInfoResponse> responses = courses.stream()
                .map(course -> CourseInfoResponse.from(course,
                        RoundUtils.roundToNDecimals(averageRatings.getOrDefault(course.getId(), 0.0), 2)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Batch endpoint for fetching multiple courses by IDs.
     * Optimized for GraphQL batch loading to avoid N+1 queries.
     */
    @PostMapping("/batch")
    public ResponseEntity<List<CourseInfoResponse>> getCoursesByIds(@RequestBody List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Batch fetch all courses efficiently using findAllById
        List<Course> courses = courseService.getCoursesByIds(courseIds);

        // Batch fetch all ratings at once
        Map<Long, Double> averageRatings = courseRatingService.getAverageRatingsByCourseIds(courseIds);

        List<CourseInfoResponse> responses = courses.stream()
                .map(course -> CourseInfoResponse.from(course,
                        RoundUtils.roundToNDecimals(averageRatings.getOrDefault(course.getId(), 0.0), 2)))
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

        public CourseInfoDto toCourseInfoDto(Long instructorId) {
            return CourseInfoDto.builder()
                    .title(this.title)
                    .description(this.description)
                    .instructorId(instructorId)
                    .build();
        }
    }
}