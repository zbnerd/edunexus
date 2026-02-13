package com.edunexuscouponservice.adapter.in.web.response;

import com.edunexuscouponservice.domain.coupon.dto.CouponValidationResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouponValidationResponse {
    private boolean valid;
    private String message;
    private Double discountAmount;

    public static CouponValidationResponse from(CouponValidationResult result) {
        return CouponValidationResponse.builder()
                .valid(result.isValid())
                .message(result.getMessage())
                .discountAmount(result.getDiscountAmount())
                .build();
    }
}
