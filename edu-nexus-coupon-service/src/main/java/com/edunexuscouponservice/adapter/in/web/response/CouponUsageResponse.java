package com.edunexuscouponservice.adapter.in.web.response;

import com.edunexuscouponservice.domain.coupon.dto.CouponUsageDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponUsageResponse {
    private Long id;
    private Long couponId;
    private Long userId;
    private Long orderId;
    private Double discountAmount;
    private LocalDateTime usedAt;

    public static CouponUsageResponse from(CouponUsageDto dto) {
        return CouponUsageResponse.builder()
                .id(dto.getId())
                .couponId(dto.getCouponId())
                .userId(dto.getUserId())
                .orderId(dto.getOrderId())
                .discountAmount(dto.getDiscountAmount())
                .usedAt(dto.getUsedAt())
                .build();
    }
}
