package com.edunexuscouponservice.domain.coupon.enums;

import lombok.Getter;

@Getter
public enum CouponStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    EXPIRED("EXPIRED");

    private final String value;

    CouponStatus(String value) {
        this.value = value;
    }
}
