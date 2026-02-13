package com.edunexuscouponservice.domain.coupon.dto;

import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    private Long id;
    private String code;
    private CouponType type;
    private Double value;
    private Double minPurchaseAmount;
    private Integer maxUsage;
    private Integer currentUsage;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private CouponStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
