package com.edunexusenrollmentservice.domain.repository;

import com.edunexusenrollmentservice.domain.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.payment WHERE e.userId = :userId AND e.courseId = :courseId")
    Optional<Enrollment> findByUserIdAndCourseId(@Param("userId") long userId, @Param("courseId") long courseId);

    Page<Enrollment> findAllByUserId(long userId, Pageable pageable);
}