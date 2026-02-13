package com.edunexuscourseservice.adapter.in.web;

import com.edunexusobservability.annotation.MetricTimed;
import com.edunexusobservability.metrics.BusinessMetrics;
import com.edunexuscourseservice.adapter.in.web.request.CourseCreateRequest;
import com.edunexuscourseservice.adapter.in.web.request.CourseUpdateRequest;
import com.edunexuscourseservice.adapter.in.web.response.CourseInfoResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseRatingAverageResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseResponse;
import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.port.in.CourseUseCase;
import com.edunexuscourseservice.domain.course.util.RoundUtils;
import com.edunexuscourseservice.port.in.CourseRatingUseCase;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
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
@Timed
public class CourseController {

    private final CourseUseCase courseUseCase;
    private final CourseRatingUseCase courseRatingService;

    /**
     * Creates a new course.
     *
     * @param request the course creation request with title, description, and instructor ID
     * @return ResponseEntity with created course and Location header
     * @throws jakarta.validation.ConstraintViolationException if request validation fails
     */
    @PostMapping
    @Counted(value = "course.creation", description = "Course creation attempts")
    public ResponseEntity<CourseResponse> createCourse(@RequestBody @jakarta.validation.Valid CourseCreateRequest request) {
        CourseInfoDto courseInfoDto = request.toCourseInfoDto();
        Course course = new Course();
        course.setCourseInfo(courseInfoDto);

        Course savedCourse = courseUseCase.saveCourse(course);
        CourseResponse response = CourseResponse.from(savedCourse);

        return ResponseEntity.created(URI.create("/courses/" + savedCourse.getId())).body(response);
    }

    /**
     * Updates an existing course's information.
     *
     * @param courseId the ID of the course to update
     * @param request the update request with new course information
     * @return ResponseEntity with updated course data
     * @throws NotFoundException if course doesn't exist
     * @throws jakarta.validation.ConstraintViolationException if request validation fails
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId,
                                                       @RequestBody @jakarta.validation.Valid CourseUpdateRequest request) {
        Course course = courseUseCase.getCourseById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));
        CourseInfoDto courseInfoDto = request.toCourseInfoDto(course.getInstructorId());

        Course newCourse = new Course();
        newCourse.setCourseInfo(courseInfoDto);
        Course updatedCourse = courseUseCase.updateCourse(course.getId(), newCourse);

        return ResponseEntity.ok(CourseResponse.from(updatedCourse));
    }

    /**
     * Retrieves detailed information for a specific course including average rating.
     *
     * @param courseId the ID of the course to retrieve
     * @return ResponseEntity with course details and average rating
     * @throws NotFoundException if course doesn't exist
     */
    @GetMapping("/{courseId}")
    @Timed(value = "course.retrieval", extraTags = {"operation", "getCourse"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<CourseInfoResponse> getCourse(@PathVariable Long courseId) {
        Course course = courseUseCase.getCourseById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        Double courseRatingAvg = courseRatingService.getAverageRatingByCourseId(courseId);


        return ResponseEntity.ok(CourseInfoResponse.from(course, RoundUtils.roundToNDecimals(courseRatingAvg, 2)));
    }

    /**
     * Retrieves the average rating for a specific course.
     *
     * @param courseId the ID of the course
     * @return ResponseEntity with course ID and average rating
     */
    @GetMapping("/{courseId}/course-rating-average")
    public ResponseEntity<CourseRatingAverageResponse> getCourseRatingAverage(
            @PathVariable Long courseId
    ) {
        Double courseRatingAvg = courseRatingService.getAverageRatingByCourseId(courseId);

        return ResponseEntity.ok(CourseRatingAverageResponse.from(courseId, courseRatingAvg));
    }

    /**
     * Retrieves a paginated list of courses with optional filtering.
     * Batch fetches ratings to avoid N+1 query problems.
     *
     * @param condition search conditions (title, description filters)
     * @param pageable pagination and sorting parameters
     * @return ResponseEntity with list of courses including average ratings
     */
    @GetMapping
    @Timed(value = "course.retrieval", extraTags = {"operation", "getAllCourses"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<List<CourseInfoResponse>> getAllCourses(
            @ModelAttribute CourseSearchCondition condition,
            Pageable pageable
    ) {
        List<Course> courses = courseUseCase.getAllCourses(condition, pageable);

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
     *
     * @param courseIds list of course IDs to retrieve
     * @return ResponseEntity with list of courses including average ratings
     */
    @PostMapping("/batch")
    @Timed(value = "course.batch_retrieval", extraTags = {"operation", "batchGetCourses"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<List<CourseInfoResponse>> getCoursesByIds(@RequestBody List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Batch fetch all courses efficiently using findAllById
        List<Course> courses = courseUseCase.getCoursesByIds(courseIds);

        // Batch fetch all ratings at once
        Map<Long, Double> averageRatings = courseRatingService.getAverageRatingsByCourseIds(courseIds);

        List<CourseInfoResponse> responses = courses.stream()
                .map(course -> CourseInfoResponse.from(course,
                        RoundUtils.roundToNDecimals(averageRatings.getOrDefault(course.getId(), 0.0), 2)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}