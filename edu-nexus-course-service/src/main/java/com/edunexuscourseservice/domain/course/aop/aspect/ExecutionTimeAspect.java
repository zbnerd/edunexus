package com.edunexuscourseservice.domain.course.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {
    @Around("@annotation(com.edunexuscourseservice.domain.course.aop.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed(); // 실제 메서드 실행

        long endTime = System.currentTimeMillis();
        log.info("Execution Time for {}: {} ms", joinPoint.getSignature().getName(), (endTime - startTime));

        return result;
    }
}
