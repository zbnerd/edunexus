package com.edunexuscourseservice.port.out;

import com.edunexuscourseservice.adapter.out.persistence.entity.redis.RCourse;
import org.springframework.data.repository.CrudRepository;

public interface CourseRedisRepository extends CrudRepository<RCourse, Long> {

}
