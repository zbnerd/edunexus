package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {

    /**
     * Find all ratings by course ID with pagination to avoid loading entire collections.
     * Use this method instead of findByCourseId() for better performance.
     */
    Page<CourseRating> findByCourseId(Long courseId, Pageable pageable);

    /**
     * Find all ratings by course ID (without pagination).
     * Consider using the paginated version for better performance.
     *
     * @deprecated Use findByCourseId(Long courseId, Pageable pageable) instead
     */
    @Deprecated
    List<CourseRating> findByCourseId(Long courseId);

    /**
     * Find rating by ID and course ID in a single query with JOIN FETCH to avoid N+1.
     * This eagerly fetches the associated course to prevent lazy loading issues.
     */
    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.course WHERE cr.id = :ratingId AND cr.course.id = :courseId")
    Optional<CourseRating> findByIdAndCourseId(@Param("ratingId") Long ratingId, @Param("courseId") Long courseId);
}
