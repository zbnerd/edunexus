package com.edunexusenrollmentservice.domain.template;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class GrpcErrorHandlingTemplate {
    private final String errorMessage;

    public <T> void execute(StreamObserver<T> responseObserver, GrpcErrorHandlingCallback callback) {
        try {
            callback.call();
        } catch (Exception e) {
            log.error(errorMessage, e);
            responseObserver.onError(e);
        }
    }
}
