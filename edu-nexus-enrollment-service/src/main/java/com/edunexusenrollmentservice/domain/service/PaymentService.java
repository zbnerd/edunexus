package com.edunexusenrollmentservice.domain.service;

import com.edunexusenrollmentservice.domain.dto.PaymentDto;
import com.edunexusenrollmentservice.domain.entity.Payment;
import com.edunexusenrollmentservice.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setPaymentInfo(paymentDto);
        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    @Transactional
    public Payment updatePaymentMethod(Long paymentId, String newPaymentMethod) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment != null) {
            payment.setPaymentMethod(newPaymentMethod);
        }
        return payment;
    }

    public List<Payment> getUserPayments(long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
