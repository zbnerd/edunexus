package com.edunexusenrollmentservice.domain.dto;

import com.edunexusenrollmentservice.domain.entity.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentDto {
    private Long userId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private String paymentMethod;
}
