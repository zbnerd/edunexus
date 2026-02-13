package com.edunexuscouponservice.adapter.in.web.response;

import com.edunexuscouponservice.domain.coupon.dto.CouponDto;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {
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

    public static CouponResponse from(CouponDto dto) {
        return CouponResponse.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .type(dto.getType())
                .value(dto.getValue())
                .minPurchaseAmount(dto.getMinPurchaseAmount())
                .maxUsage(dto.getMaxUsage())
                .currentUsage(dto.getCurrentUsage())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
