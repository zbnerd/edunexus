package com.edunexusenrollmentservice.domain.grpc;

import com.edunexusenrollmentservice.domain.dto.PaymentDto;
import com.edunexusenrollmentservice.domain.entity.Payment;
import com.edunexusenrollmentservice.domain.entity.PaymentType;
import com.edunexusenrollmentservice.domain.service.EnrollmentServiceOuterClass;
import com.edunexusenrollmentservice.domain.service.FakePaymentServiceGrpc;
import com.edunexusenrollmentservice.domain.service.PaymentService;
import com.edunexusenrollmentservice.domain.template.GrpcErrorHandlingTemplate;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class PaymentGrpcService extends FakePaymentServiceGrpc.FakePaymentServiceImplBase {

    private final PaymentService paymentService;

    @Override
    public void createPayment(
            EnrollmentServiceOuterClass.PaymentRequest request,
            StreamObserver<EnrollmentServiceOuterClass.PaymentResponse> responseObserver
    ) {

        GrpcErrorHandlingTemplate template = new GrpcErrorHandlingTemplate("payment error");

        template.execute(responseObserver, () -> {
            Payment payment = paymentService.createPayment(
                    PaymentDto.builder()
                            .userId(request.getUserId())
                            .paymentType(PaymentType.valueOf(request.getType()))
                            .amount(BigDecimal.valueOf(request.getAmount()))
                            .build()
            );

            responseObserver.onNext(payment.toProto());
            responseObserver.onCompleted();
        });
    }

    @Override
    public void listUserPayments(
            EnrollmentServiceOuterClass.UserPaymentsRequest request,
            StreamObserver<EnrollmentServiceOuterClass.UserPaymentsResponse> responseObserver
    ) {
        List<Payment> payments = paymentService.getUserPayments(request.getUserId());
        EnrollmentServiceOuterClass.UserPaymentsResponse response = EnrollmentServiceOuterClass.UserPaymentsResponse.newBuilder()
                .addAllPayments(payments.stream().map(Payment::toProto).toList())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPaymentsByPaymentId(
            EnrollmentServiceOuterClass.PaymentsByIdRequest request,
            StreamObserver<EnrollmentServiceOuterClass.PaymentsByIdResponse> responseObserver
    ) {
        Payment payment = paymentService.getPaymentById(request.getPaymentId());
        EnrollmentServiceOuterClass.PaymentsByIdResponse response = EnrollmentServiceOuterClass.PaymentsByIdResponse.newBuilder()
                .setPayment(payment.toProto())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
