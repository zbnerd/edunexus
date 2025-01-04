package com.edunexuscourseservice.adapter.out.persistence.entity;

import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Entity
@ToString
@Table(name = "COURSE_RATINGS")
public class CourseRating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(nullable = false)
    private int rating;

    @Setter
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