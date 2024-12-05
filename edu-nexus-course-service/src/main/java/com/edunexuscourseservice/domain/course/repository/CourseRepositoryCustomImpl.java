package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.condition.CourseSearch;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
    public List<Course> findAll(CourseSearch courseSearch, Pageable pageable) {
        return queryFactory
                .selectFrom(course)
                .where(courseNameEq(courseSearch.getCourseTitle()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression courseNameEq(String courseTitle) {
        if (!StringUtils.hasText(courseTitle)) {
            return null;
        }
        return course.title.toLowerCase().like("%" + courseTitle.toLowerCase() + "%");
    }
}
