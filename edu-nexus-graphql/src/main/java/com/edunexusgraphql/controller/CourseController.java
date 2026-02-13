package com.edunexusgraphql.controller;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseRating;
import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @QueryMapping
    public List<Course> listCourses(
            @Argument String title,
            @Argument String description,
            @Argument Integer page
    ) {
        return courseService.findAllCourses(title, description, page);
    }

    @QueryMapping
    public Course getCourse(@Argument Long userId, @Argument Long courseId) {
        return courseService.findCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    }

    @QueryMapping
    public List<CourseSession> listCourseSessions(@Argument Long courseId) {
        return courseService.findAllSessionsByCourseId(courseId);
    }

    @MutationMapping
    public Course createCourse(
            @Argument String title,
            @Argument String description,
            @Argument Long instructorId
    ) {
        return courseService.createCourse(title, description, instructorId);
    }

    @MutationMapping
    public Course updateCourse(
            @Argument Long id,
            @Argument String title,
            @Argument String description
    ) {
        return courseService.updateCourse(id, title, description);
    }

    @MutationMapping
    public CourseSession addCourseSession(
            @Argument Long courseId,
            @Argument String title
    ) {
        return courseService.addSessionToCourse(courseId, title);
    }

    @MutationMapping
    public CourseRating rateCourse(
            @Argument Long userId,
            @Argument Long courseId,
            @Argument Integer rating,
            @Argument String comment
    ) {
        return courseService.addRatingToCourse(userId, courseId, rating, comment);
    }

}
