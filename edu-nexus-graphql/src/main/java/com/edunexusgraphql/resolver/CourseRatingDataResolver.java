package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseRating;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.dummy.DummyCourseService;
import com.edunexusgraphql.service.dummy.DummyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CourseRatingDataResolver {

    private final DummyCourseService courseService;
    private final DummyUserService userService;

    @SchemaMapping(typeName = "CourseRating", field = "course")
    public Course getCourse(CourseRating rating) {
        return courseService.findCourseById(rating.getCourseId()).orElseThrow();
    }

    @SchemaMapping
    public User getUser(CourseRating rating) {
        return userService.findById(rating.getUserId()).orElseThrow();
    }
}
