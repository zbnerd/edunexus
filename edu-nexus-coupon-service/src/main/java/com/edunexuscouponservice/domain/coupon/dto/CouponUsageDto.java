package com.edunexuscouponservice.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageDto {
    private Long id;
    private Long couponId;
    private Long userId;
    private Long orderId;
    private Double discountAmount;
    private LocalDateTime usedAt;
}
