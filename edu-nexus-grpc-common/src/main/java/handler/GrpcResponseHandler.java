package handler;

import io.grpc.stub.StreamObserver;

import java.util.Optional;
import java.util.function.Function;

public class GrpcResponseHandler {

    public static <T> void sendResponse(T response, StreamObserver<T> responseObserver) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public static <T, R> void handleOptional(
            Optional<T> optional,
            Function<T, R> processLogic,
            StreamObserver<R> responseObserver
    ) {
        optional.ifPresentOrElse(
            entity -> {
                R response = processLogic.apply(entity);
                GrpcResponseHandler.sendResponse(response, responseObserver);
            },
            () -> responseObserver.onError(new Throwable(optional.map(entityType -> entityType.getClass().getName()).orElse("") + " Not Found"))
        );
    }

}
