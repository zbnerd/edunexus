package com.edunexuscouponservice.domain.coupon.enums;

import lombok.Getter;

@Getter
public enum CouponType {
    PERCENTAGE("PERCENTAGE"),
    FIXED_AMOUNT("FIXED_AMOUNT");

    private final String value;

    CouponType(String value) {
        this.value = value;
    }
}
