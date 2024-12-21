package com.edunexusgraphql.resolver;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.dummy.DummyCourseService;
import com.edunexusgraphql.service.dummy.DummyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourseDataResolver {

    private final DummyCourseService courseService;
    private final DummyUserService userService;

    @SchemaMapping(typeName = "Course", field = "courseSessions")
    public List<CourseSession> getSessions(Course course) {
        return courseService.findAllSessionsByCourseId(course.getId());
    }

    @SchemaMapping(typeName = "Course", field = "instructor")
    public User getInstructor(Course course) {
        return userService.findById(course.getInstructorId()).orElseThrow();
    }
}
