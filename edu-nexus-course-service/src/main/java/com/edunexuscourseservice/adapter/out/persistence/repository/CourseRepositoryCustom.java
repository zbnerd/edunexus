package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseRepositoryCustom {
    List<Course> findAll(CourseSearchCondition condition, Pageable pageable);
}
