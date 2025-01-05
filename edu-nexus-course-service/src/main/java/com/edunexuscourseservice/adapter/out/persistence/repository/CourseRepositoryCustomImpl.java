package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.context.CourseSearchConditionContext;
import com.edunexuscourseservice.port.out.CourseRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.edunexuscourseservice.adapter.out.persistence.entity.QCourse.course;

@Repository
public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final CourseSearchConditionContext conditionContext;

    public CourseRepositoryCustomImpl(EntityManager em, CourseSearchConditionContext conditionContext) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
        this.conditionContext = conditionContext;
    }

    @Override
    public List<Course> findAll(CourseSearchCondition condition, Pageable pageable) {
        return queryFactory
                .selectFrom(course)
                .where(conditionContext.buildExpression(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
