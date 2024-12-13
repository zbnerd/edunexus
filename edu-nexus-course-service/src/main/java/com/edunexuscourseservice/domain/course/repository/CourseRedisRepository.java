package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.entity.redis.RCourse;
import org.springframework.data.repository.CrudRepository;

public interface CourseRedisRepository extends CrudRepository<RCourse, Long> {

}
