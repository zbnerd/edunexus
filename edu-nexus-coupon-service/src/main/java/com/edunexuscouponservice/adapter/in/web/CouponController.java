package com.edunexuscouponservice.adapter.in.web;

import com.edunexuscouponservice.adapter.in.web.request.CouponApplyRequest;
import com.edunexuscouponservice.adapter.in.web.request.CouponCreateRequest;
import com.edunexuscouponservice.adapter.in.web.response.CouponResponse;
import com.edunexuscouponservice.adapter.in.web.response.CouponUsageResponse;
import com.edunexuscouponservice.adapter.in.web.response.CouponValidationResponse;
import com.edunexuscouponservice.domain.coupon.dto.*;
import com.edunexuscouponservice.port.in.CouponUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponUseCase couponUseCase;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        CreateCouponRequest createRequest = request.toCreateCouponRequest();
        CouponDto couponDto = couponUseCase.createCoupon(createRequest);
        return ResponseEntity.created(URI.create("/coupons/" + couponDto.getId()))
                .body(CouponResponse.from(couponDto));
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        List<CouponDto> coupons = couponUseCase.getAllCoupons();
        return ResponseEntity.ok(coupons.stream()
                .map(CouponResponse::from)
                .toList());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getAllActiveCoupons() {
        List<CouponDto> coupons = couponUseCase.getAllActiveCoupons();
        return ResponseEntity.ok(coupons.stream()
                .map(CouponResponse::from)
                .toList());
    }

    @GetMapping("/valid")
    public ResponseEntity<List<CouponResponse>> getValidCoupons() {
        List<CouponDto> coupons = couponUseCase.getValidCoupons();
        return ResponseEntity.ok(coupons.stream()
                .map(CouponResponse::from)
                .toList());
    }

    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        CouponDto couponDto = couponUseCase.getCouponByCode(code);
        return ResponseEntity.ok(CouponResponse.from(couponDto));
    }

    @PostMapping("/{code}/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @PathVariable String code,
            @RequestParam Double orderAmount) {
        CouponValidationResult result = couponUseCase.validateCoupon(code, orderAmount);
        return ResponseEntity.ok(CouponValidationResponse.from(result));
    }

    @PostMapping("/{code}/apply")
    public ResponseEntity<CouponValidationResponse> applyCoupon(
            @PathVariable String code,
            @Valid @RequestBody CouponApplyRequest request) {
        ApplyCouponRequest applyRequest = request.toApplyCouponRequest();
        CouponValidationResult result = couponUseCase.applyCoupon(code, applyRequest);
        return ResponseEntity.ok(CouponValidationResponse.from(result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CouponUsageResponse>> getUserCouponUsage(@PathVariable Long userId) {
        List<CouponUsageDto> usages = couponUseCase.getUserCouponUsage(userId);
        return ResponseEntity.ok(usages.stream()
                .map(CouponUsageResponse::from)
                .toList());
    }

    @PatchMapping("/{couponId}/status")
    public ResponseEntity<CouponResponse> updateCouponStatus(
            @PathVariable Long couponId,
            @RequestParam String status) {
        CouponDto couponDto = couponUseCase.updateCouponStatus(couponId, status);
        return ResponseEntity.ok(CouponResponse.from(couponDto));
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {
        couponUseCase.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }
}
