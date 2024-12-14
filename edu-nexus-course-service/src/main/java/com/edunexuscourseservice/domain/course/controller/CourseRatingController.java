package com.edunexuscourseservice.domain.course.controller;

import com.edunexuscourseservice.domain.course.controller.response.CourseRatingAverageResponse;
import com.edunexuscourseservice.domain.course.controller.response.CourseRatingResponse;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import com.edunexuscourseservice.domain.course.entity.CourseRating;
import com.edunexuscourseservice.domain.course.service.CourseRatingService;
import com.edunexuscourseservice.domain.course.util.RoundUtils;
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

    private final CourseRatingService courseRatingService;

    // 강의 평가 추가
    @PostMapping
    public ResponseEntity<CourseRatingResponse> addRating(@PathVariable Long courseId,
                                                          @RequestBody CourseRatingCreateRequest request) {
        CourseRating courseRating = courseRatingService.addRatingToCourse(courseId, request.toEntity());
        CourseRatingResponse response = CourseRatingResponse.from(courseRating);

        return ResponseEntity.created(URI.create("/courses/" + courseId + "/ratings/" + courseRating.getId()))
                .body(response);
    }

    // 강의 평가 업데이트
    @PutMapping("/{ratingId}")
    public ResponseEntity<CourseRatingResponse> updateRating(
            @PathVariable Long ratingId,
            @RequestBody CourseRatingUpdateRequest request
    ) {
        CourseRating updatedRating = courseRatingService.updateRating(ratingId, request.toEntity());
        return ResponseEntity.ok(CourseRatingResponse.from(updatedRating));
    }

    // 강의 평가 삭제
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long ratingId
    ) {
        courseRatingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }

    // 특정 강의의 모든 평가 조회
    @GetMapping
    public ResponseEntity<List<CourseRatingResponse>> getAllRatings(@PathVariable Long courseId) {
        List<CourseRating> ratings = courseRatingService.getAllRatingsByCourseId(courseId);
        List<CourseRatingResponse> responses = ratings.stream()
                .map(CourseRatingResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // 특정 강의의 평점 평균 조회
    @GetMapping("/average")
    public ResponseEntity<CourseRatingAverageResponse> getAverageRating(@PathVariable Long courseId) {
        Double averageRating = RoundUtils.roundToNDecimals(courseRatingService.getAverageRatingByCourseId(courseId), 2);
        CourseRatingAverageResponse response = CourseRatingAverageResponse.builder()
                .averageRating(averageRating)
                .courseId(courseId)
                .build();

        return ResponseEntity.ok(response);
    }

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
