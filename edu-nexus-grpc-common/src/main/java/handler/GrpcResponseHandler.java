package handler;

import io.grpc.stub.StreamObserver;

import java.util.Optional;
import java.util.function.Function;

public class GrpcResponseHandler {

    public static <T> void sendResponse(T response, StreamObserver<T> responseObserver) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
