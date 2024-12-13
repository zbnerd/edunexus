package com.edunexuscourseservice.domain.course.entity.redis;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.util.RedisKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@RedisHash("edu-nexus-course:course:entity")
@NoArgsConstructor
public class RCourse implements Serializable {

    @Id
    private Long id;
    private String title;
    private String description;
    private Long instructorId;

    public RCourse(Long id, String title, String description, Long instructorId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
    }

    public RCourse(Course course) {
        this(course.getId(), course.getTitle(), course.getDescription(), course.getInstructorId());
    }
}
