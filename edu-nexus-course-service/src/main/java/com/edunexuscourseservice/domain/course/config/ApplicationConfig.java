package com.edunexuscourseservice.domain.course.config;

import com.edunexuscourseservice.domain.course.entity.condition.strategy.SearchStrategy;
import com.edunexuscourseservice.domain.course.entity.condition.strategy.TitleSearchStrategy;

public abstract class ApplicationConfig {

    public static SearchStrategy getTitleSearchStrategy() {
        return new TitleSearchStrategy();
    }
}
