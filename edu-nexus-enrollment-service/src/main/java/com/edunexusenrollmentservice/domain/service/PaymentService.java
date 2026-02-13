package com.edunexusenrollmentservice.domain.service;

import com.edunexusenrollmentservice.domain.dto.PaymentDto;
import com.edunexusenrollmentservice.domain.entity.Payment;
import com.edunexusenrollmentservice.domain.entity.PaymentType;
import com.edunexusenrollmentservice.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Create a payment with explicit parameters.
     * Used by Saga orchestrator for distributed transaction coordination.
     *
     * @param userId The user ID
     * @param courseId The course ID (for reference)
     * @param amount The payment amount as string
     * @return The created payment
     */
    @Transactional
    public Payment createPayment(Long userId, Long courseId, String amount) {
        PaymentDto dto = PaymentDto.builder()
                .userId(userId)
                .amount(new java.math.BigDecimal(amount))
                .paymentMethod("CARD")  // Default payment method
                .paymentType(PaymentType.COURSE)
                .build();

        Payment payment = new Payment();
        payment.setPaymentInfo(dto);
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Transactional
    public Optional<Payment> updatePaymentMethod(Long paymentId, String newPaymentMethod) {
        return paymentRepository.findById(paymentId)
                .map(payment -> {
                    payment.setPaymentMethod(newPaymentMethod);
                    return paymentRepository.save(payment);
                });
    }

    public List<Payment> getUserPayments(long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
