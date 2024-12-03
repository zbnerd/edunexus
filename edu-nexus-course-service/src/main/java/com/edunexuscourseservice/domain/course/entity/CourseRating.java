package com.edunexuscourseservice.domain.course.entity;

import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "COURSE_RATINGS")
public class CourseRating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int rating;

    @Column
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @Setter
    private Course course;

    public void setCourseRatingInfo(CourseRatingInfoDto courseRatingInfoDto) {
        this.userId = courseRatingInfoDto.getUserId();
        this.rating = courseRatingInfoDto.getRating();
        this.comment = courseRatingInfoDto.getComment();
    }

    public void updateCourseRating(CourseRating newCourseRating) {
        this.rating = newCourseRating.getRating();
        this.comment = newCourseRating.getComment();
    }
}