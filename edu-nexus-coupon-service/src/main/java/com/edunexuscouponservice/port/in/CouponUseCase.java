package com.edunexuscouponservice.port.in;

import com.edunexuscouponservice.domain.coupon.dto.*;

import java.util.List;

public interface CouponUseCase {
    CouponDto createCoupon(CreateCouponRequest request);
    CouponDto getCouponByCode(String code);
    List<CouponDto> getAllActiveCoupons();
    List<CouponDto> getAllCoupons();
    CouponValidationResult validateCoupon(String code, Double orderAmount);
    CouponValidationResult applyCoupon(String code, ApplyCouponRequest request);
    List<CouponUsageDto> getUserCouponUsage(Long userId);
    List<CouponDto> getValidCoupons();
    CouponDto updateCouponStatus(Long couponId, String status);
    void deleteCoupon(Long couponId);
}
