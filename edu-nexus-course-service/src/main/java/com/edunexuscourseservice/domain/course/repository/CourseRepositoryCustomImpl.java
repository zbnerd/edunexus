package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.condition.CourseSearch;
import com.edunexuscourseservice.domain.course.entity.condition.strategy.SearchStrategy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.edunexuscourseservice.domain.course.entity.QCourse.course;

@Repository
public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public CourseRepositoryCustomImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Course> findAll(CourseSearch search, SearchStrategy searchStrategy, Pageable pageable) {
        return queryFactory
                .selectFrom(course)
                .where(searchStrategy.getSearchCondition(search))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
