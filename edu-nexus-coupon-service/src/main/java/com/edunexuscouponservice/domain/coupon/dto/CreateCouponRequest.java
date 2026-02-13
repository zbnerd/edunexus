package com.edunexuscouponservice.domain.coupon.dto;

import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {
    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    private String code;

    @NotNull(message = "Coupon type is required")
    private CouponType type;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Double value;

    @PositiveOrZero(message = "Minimum purchase amount must be positive or zero")
    private Double minPurchaseAmount;

    @Positive(message = "Max usage must be positive")
    private Integer maxUsage;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    @NotNull(message = "Status is required")
    private CouponStatus status;

    public Double getMinPurchaseAmount() {
        return minPurchaseAmount != null ? minPurchaseAmount : 0.0;
    }
}
