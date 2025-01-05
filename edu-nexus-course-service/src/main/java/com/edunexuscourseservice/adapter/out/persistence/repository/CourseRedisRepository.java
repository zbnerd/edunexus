package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.redis.RCourse;
import org.springframework.data.repository.CrudRepository;

public interface CourseRedisRepository extends CrudRepository<RCourse, Long> {

}
