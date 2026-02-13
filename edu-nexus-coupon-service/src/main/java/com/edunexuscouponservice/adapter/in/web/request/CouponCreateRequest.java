package com.edunexuscouponservice.adapter.in.web.request;

import com.edunexuscouponservice.domain.coupon.dto.CreateCouponRequest;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponCreateRequest {
    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    private String code;

    @NotNull(message = "Coupon type is required")
    private CouponType type;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Double value;

    private Double minPurchaseAmount;

    @Positive(message = "Max usage must be positive")
    private Integer maxUsage;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    @NotNull(message = "Status is required")
    private CouponStatus status;

    public CreateCouponRequest toCreateCouponRequest() {
        return CreateCouponRequest.builder()
                .code(code)
                .type(type)
                .value(value)
                .minPurchaseAmount(minPurchaseAmount)
                .maxUsage(maxUsage)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .status(status)
                .build();
    }
}
