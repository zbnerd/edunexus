package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingAddResponseEvent {
    private String response; // 성공 또는 실패 상태 ("success", "fail")
    private String method;   // 호출된 메서드명 ("CourseRatingAddEvent")
    private Long courseId;   // 코스 ID
    private int rating;      // 코스의 평점
    private Long courseRatingId;
}