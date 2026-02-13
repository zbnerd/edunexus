package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

    /**
     * Find course by ID with sessions eagerly loaded using EntityGraph.
     * Use this when you need to access course sessions to avoid N+1 queries.
     */
    @EntityGraph(attributePaths = {"sessions"})
    Optional<Course> findWithSessionsById(Long id);

    /**
     * Find course by ID with ratings eagerly loaded using EntityGraph.
     * Use this when you need to access course ratings to avoid N+1 queries.
     */
    @EntityGraph(attributePaths = {"ratings"})
    Optional<Course> findWithRatingsById(Long id);

    /**
     * Find course by ID with both sessions and ratings eagerly loaded using EntityGraph.
     * Use this when you need to access both to avoid N+1 queries.
     */
    @EntityGraph(attributePaths = {"sessions", "ratings"})
    Optional<Course> findWithSessionsAndRatingsById(Long id);

    /**
     * Find course by ID with sessions using JOIN FETCH in a single query.
     * This is an alternative to EntityGraph for eager loading.
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.sessions WHERE c.id = :id")
    Optional<Course> findWithSessionsFetch(@Param("id") Long id);

    /**
     * Find course by ID with ratings using JOIN FETCH in a single query.
     * This is an alternative to EntityGraph for eager loading.
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.ratings WHERE c.id = :id")
    Optional<Course> findWithRatingsFetch(@Param("id") Long id);
}
