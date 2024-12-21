package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseRating;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.CourseService;
import com.edunexusgraphql.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CourseRatingDataResolver {

    private final CourseService courseService;
    private final UserService userService;

    @SchemaMapping(typeName = "CourseRating", field = "course")
    public Course getCourse(CourseRating rating) {
        return courseService.findCourseById(rating.getCourseId()).orElseThrow();
    }

    @SchemaMapping(typeName = "CourseRating", field = "user")
    public User getUser(CourseRating rating) {
        return userService.findById(rating.getUserId()).orElseThrow();
    }

}
