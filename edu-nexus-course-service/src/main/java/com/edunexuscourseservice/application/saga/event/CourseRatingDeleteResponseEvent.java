package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingDeleteResponseEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private String response; // 성공 또는 실패 상태 ("success", "fail")
    private String method;   // 호출된 메서드명 ("CourseRatingDeleteEvent")
    private Long courseId;   // 코스 ID
    private int oldRating;   // 이전 평점
}