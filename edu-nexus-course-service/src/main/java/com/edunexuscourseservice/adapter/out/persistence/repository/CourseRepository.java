package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.port.out.CourseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {
}
