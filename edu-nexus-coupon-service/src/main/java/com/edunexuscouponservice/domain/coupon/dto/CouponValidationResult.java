package com.edunexuscouponservice.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResult {
    private boolean valid;
    private String message;
    private Double discountAmount;

    public static CouponValidationResult valid(Double discountAmount) {
        return CouponValidationResult.builder()
                .valid(true)
                .message("Coupon is valid")
                .discountAmount(discountAmount)
                .build();
    }

    public static CouponValidationResult invalid(String reason) {
        return CouponValidationResult.builder()
                .valid(false)
                .message(reason)
                .discountAmount(0.0)
                .build();
    }
}
