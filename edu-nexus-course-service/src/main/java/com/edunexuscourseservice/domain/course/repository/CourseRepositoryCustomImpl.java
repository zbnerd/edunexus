package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.domain.course.entity.condition.context.CourseSearchConditionContext;
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

    private BooleanExpression courseTitleLike(String courseTitle) {
        if (StringUtils.hasText(courseTitle)) return course.title.like("%" + courseTitle + "%");
        return null;
    }
}
