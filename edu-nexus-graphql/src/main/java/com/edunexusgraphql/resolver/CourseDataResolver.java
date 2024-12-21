package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.CourseService;
import com.edunexusgraphql.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CourseDataResolver {

    private final CourseService courseService;
    private final UserService userService;

    @SchemaMapping(typeName = "Course", field = "courseSessions")
    public List<CourseSession> getSessions(Course course) {
        return courseService.findAllSessionsByCourseId(course.getId());
    }

    @SchemaMapping(typeName = "Course", field = "instructor")
    public User getInstructor(Course course) {
        return userService.findById(course.getInstructorId()).orElseThrow();
    }


}
