package com.edunexuscourseservice.adapter.in.web;

import com.edunexuscourseservice.adapter.in.web.response.CourseRatingAverageResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseRatingResponse;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.domain.course.util.RoundUtils;
import com.edunexuscourseservice.port.in.CourseRatingUseCase;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses/{courseId}/ratings")
@RequiredArgsConstructor
public class CourseRatingController {

    private final CourseRatingUseCase courseRatingService;

    /**
     * Adds a new rating to a course.
     *
     * @param courseId the ID of the course to rate
     * @param request the rating request with user ID, score (1-5), and optional comment
     * @return ResponseEntity with created rating and Location header
     */
    @PostMapping
    public ResponseEntity<CourseRatingResponse> addRating(@PathVariable Long courseId,
                                                          @RequestBody CourseRatingCreateRequest request) {
        CourseRating courseRating = courseRatingService.addRatingToCourse(courseId, request.toEntity());
        CourseRatingResponse response = CourseRatingResponse.from(courseRating);

        return ResponseEntity.created(URI.create("/courses/" + courseId + "/ratings/" + courseRating.getId()))
                .body(response);
    }

    /**
     * Updates an existing course rating.
     *
     * @param ratingId the ID of the rating to update
     * @param request the update request with new score and/or comment
     * @return ResponseEntity with updated rating
     * @throws NotFoundException if rating doesn't exist
     */
    @PutMapping("/{ratingId}")
    public ResponseEntity<CourseRatingResponse> updateRating(
            @PathVariable Long ratingId,
            @RequestBody CourseRatingUpdateRequest request
    ) {
        CourseRating updatedRating = courseRatingService.updateRating(ratingId, request.toEntity());
        return ResponseEntity.ok(CourseRatingResponse.from(updatedRating));
    }

    /**
     * Deletes a course rating.
     *
     * @param ratingId the ID of the rating to delete
     * @return ResponseEntity with no content (204)
     * @throws NotFoundException if rating doesn't exist
     */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long ratingId
    ) {
        courseRatingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all ratings for a specific course.
     *
     * @param courseId the ID of the course
     * @return ResponseEntity with list of all ratings
     */
    @GetMapping
    public ResponseEntity<List<CourseRatingResponse>> getAllRatings(@PathVariable Long courseId) {
        List<CourseRating> ratings = courseRatingService.getAllRatingsByCourseId(courseId);
        List<CourseRatingResponse> responses = ratings.stream()
                .map(CourseRatingResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves the average rating for a specific course from cache.
     *
     * @param courseId the ID of the course
     * @return ResponseEntity with course ID and average rating rounded to 2 decimals
     */
    @GetMapping("/average")
    public ResponseEntity<CourseRatingAverageResponse> getAverageRating(@PathVariable Long courseId) {
        Double averageRating = RoundUtils.roundToNDecimals(courseRatingService.getAverageRatingByCourseId(courseId), 2);
        CourseRatingAverageResponse response = CourseRatingAverageResponse.builder()
                .averageRating(averageRating)
                .courseId(courseId)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the average rating directly from the database.
     * This bypasses the cache and is useful for debugging or cache comparison.
     *
     * @param courseId the ID of the course
     * @return ResponseEntity with course ID and average rating from DB
     * @throws NotFoundException if course has no ratings
     */
    @GetMapping("/average/db")
    public ResponseEntity<CourseRatingAverageResponse> getAverageRatingFromDb(@PathVariable Long courseId) {
        List<CourseRating> courseRatingList = courseRatingService.getAllRatingsByCourseId(courseId);
        Double averageRating = RoundUtils.roundToNDecimals(courseRatingList.stream().mapToInt(CourseRating::getRating).average().getAsDouble(), 2);
        CourseRatingAverageResponse response = CourseRatingAverageResponse.builder()
                .averageRating(averageRating)
                .courseId(courseId)
                .build();

        return ResponseEntity.ok(response);
    }

    @Getter
    @Builder
    static class CourseRatingCreateRequest {
        private Long userId;
        private int rating;
        private String comment;

        public CourseRating toEntity() {
            CourseRating courseRating = new CourseRating();
            courseRating.setCourseRatingInfo(CourseRatingInfoDto.builder()
                    .userId(userId)
                    .rating(rating)
                    .comment(comment)
                    .build());
            return courseRating;
        }
    }

    @Getter
    @Builder
    static class CourseRatingUpdateRequest {
        private int rating;
        private String comment;

        public CourseRating toEntity() {
            CourseRating courseRating = new CourseRating();
            courseRating.setCourseRatingInfo(CourseRatingInfoDto.builder()
                    .rating(rating)
                    .comment(comment)
                    .build());
            return courseRating;
        }
    }
}
