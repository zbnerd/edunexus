package com.edunexuscourseservice.domain.course.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "COURSE_SESSIONS")
public class CourseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @Setter
    private Course course;

    public void setCourseSessionInfo(String courseSessionTitle) {
        this.title = courseSessionTitle;
    }

    public void updateCourseSession(CourseSession newCourseSession) {
        this.title = newCourseSession.title;
    }
}