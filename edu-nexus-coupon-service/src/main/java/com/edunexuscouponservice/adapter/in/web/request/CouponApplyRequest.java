package com.edunexuscouponservice.adapter.in.web.request;

import com.edunexuscouponservice.domain.coupon.dto.ApplyCouponRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CouponApplyRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order amount is required")
    @Positive(message = "Order amount must be positive")
    private Double orderAmount;

    private Long orderId;

    public ApplyCouponRequest toApplyCouponRequest() {
        return ApplyCouponRequest.builder()
                .userId(userId)
                .orderAmount(orderAmount)
                .orderId(orderId)
                .build();
    }
}
