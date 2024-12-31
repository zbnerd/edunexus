package com.edunexuscourseservice.adapter.out.persistence.entity;

import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "COURSES")
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @OneToMany(mappedBy = "course")
    @JsonBackReference
    private List<CourseSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @JsonBackReference
    private List<CourseRating> ratings = new ArrayList<>();

    public void setCourseInfo(CourseInfoDto courseInfoDto) {
        this.title = courseInfoDto.getTitle();
        this.description = courseInfoDto.getDescription();
        this.instructorId = courseInfoDto.getInstructorId();
    }

    // 업데이트 메서드
    public void updateCourse(Course newCourse) {
        title = newCourse.getTitle();
        description = newCourse.getDescription();
        instructorId = newCourse.getInstructorId();
    }
}