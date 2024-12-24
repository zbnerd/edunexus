package com.edunexusenrollmentservice.domain.aop;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class GrpcMethodAspect {

    @Around("execution(* com.edunexusenrollmentservice.domain.grpc.*.*(..))")
    public Object handleGrpcMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        StreamObserver<?> responseObserver = (StreamObserver<?>) args[args.length - 1];

        try {
            // 메서드 실행
            Object result = joinPoint.proceed();
            responseObserver.onCompleted();
            return result;
        } catch (Exception ex) {
            log.error("gRPC error in method: " + joinPoint.getSignature().toShortString(), ex);
            responseObserver.onError(ex);
            return null;
        }
    }

}
