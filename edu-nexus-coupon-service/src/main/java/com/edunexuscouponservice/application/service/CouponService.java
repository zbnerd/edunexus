package com.edunexuscouponservice.application.service;

import com.edunexus.common.exception.BusinessException;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscouponservice.adapter.out.persistence.entity.Coupon;
import com.edunexuscouponservice.adapter.out.persistence.entity.CouponUsage;
import com.edunexuscouponservice.adapter.out.persistence.repository.CouponRepository;
import com.edunexuscouponservice.adapter.out.persistence.repository.CouponUsageRepository;
import com.edunexuscouponservice.domain.coupon.dto.*;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.port.in.CouponUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService implements CouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Override
    @Transactional
    public CouponDto createCoupon(CreateCouponRequest request) {
        log.info("Creating coupon with code: {}", request.getCode());

        couponRepository.findByCode(request.getCode()).ifPresent(existing -> {
            throw new BusinessException("Coupon with code " + request.getCode() + " already exists");
        });

        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new BusinessException("Valid until date must be after valid from date");
        }

        if (request.getType() == com.edunexuscouponservice.domain.coupon.enums.CouponType.PERCENTAGE
                && (request.getValue() <= 0 || request.getValue() > 100)) {
            throw new BusinessException("Percentage discount must be between 0 and 100");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .type(request.getType())
                .value(request.getValue())
                .minPurchaseAmount(request.getMinPurchaseAmount())
                .maxUsage(request.getMaxUsage())
                .currentUsage(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .status(request.getStatus())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon created successfully with id: {}", savedCoupon.getId());

        return toDto(savedCoupon);
    }

    @Override
    public CouponDto getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Coupon not found with code: " + code));
        return toDto(coupon);
    }

    @Override
    public List<CouponDto> getAllActiveCoupons() {
        Page<Coupon> couponPage = couponRepository.findByStatus(CouponStatus.ACTIVE, Pageable.unpaged());
        return couponPage.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponDto> getValidCoupons() {
        Page<Coupon> couponPage = couponRepository.findValidCoupons(LocalDateTime.now(), Pageable.unpaged());
        return couponPage.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CouponValidationResult validateCoupon(String code, Double orderAmount) {
        log.info("Validating coupon: {} for order amount: {}", code, orderAmount);

        return couponRepository.findByCode(code)
                .map(coupon -> validateCoupon(coupon, orderAmount))
                .orElse(CouponValidationResult.invalid("Coupon not found"));
    }

    private CouponValidationResult validateCoupon(Coupon coupon, Double orderAmount) {

        if (!coupon.isValid()) {
            if (coupon.getStatus() != CouponStatus.ACTIVE) {
                return CouponValidationResult.invalid("Coupon is not active");
            }
            if (LocalDateTime.now().isBefore(coupon.getValidFrom())) {
                return CouponValidationResult.invalid("Coupon is not yet valid");
            }
            if (LocalDateTime.now().isAfter(coupon.getValidUntil())) {
                return CouponValidationResult.invalid("Coupon has expired");
            }
            if (coupon.getMaxUsage() != null && coupon.getCurrentUsage() >= coupon.getMaxUsage()) {
                return CouponValidationResult.invalid("Coupon usage limit reached");
            }
        }

        if (!coupon.canApplyToAmount(orderAmount)) {
            return CouponValidationResult.invalid(
                    "Minimum purchase amount of " + coupon.getMinPurchaseAmount() + " required"
            );
        }

        Double discountAmount = coupon.calculateDiscount(orderAmount);
        return CouponValidationResult.valid(discountAmount);
    }

    @Override
    @Transactional
    public CouponValidationResult applyCoupon(String code, ApplyCouponRequest request) {
        log.info("Applying coupon: {} for user: {} with order amount: {}",
                code, request.getUserId(), request.getOrderAmount());

        CouponValidationResult validationResult = validateCoupon(code, request.getOrderAmount());

        if (!validationResult.isValid()) {
            log.warn("Coupon validation failed: {}", validationResult.getMessage());
            return validationResult;
        }

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Coupon not found with code: " + code));

        coupon.incrementUsage();
        couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder()
                .couponId(coupon.getId())
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .discountAmount(validationResult.getDiscountAmount())
                .usedAt(LocalDateTime.now())
                .build();

        couponUsageRepository.save(usage);
        log.info("Coupon applied successfully. Discount: {}", validationResult.getDiscountAmount());

        return validationResult;
    }

    @Override
    public List<CouponUsageDto> getUserCouponUsage(Long userId) {
        List<CouponUsage> usages = couponUsageRepository.findByUserId(userId);
        return usages.stream()
                .map(this::toUsageDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CouponDto updateCouponStatus(Long couponId, String status) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundException("Coupon not found with id: " + couponId));

        try {
            CouponStatus newStatus = CouponStatus.valueOf(status.toUpperCase());
            coupon.setStatus(newStatus);
            Coupon updated = couponRepository.save(coupon);
            return toDto(updated);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid coupon status: " + status);
        }
    }

    @Override
    @Transactional
    public void deleteCoupon(Long couponId) {
        if (!couponRepository.existsById(couponId)) {
            throw new NotFoundException("Coupon not found with id: " + couponId);
        }
        couponRepository.deleteById(couponId);
        log.info("Coupon deleted with id: {}", couponId);
    }

    private CouponDto toDto(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType())
                .value(coupon.getValue())
                .minPurchaseAmount(coupon.getMinPurchaseAmount())
                .maxUsage(coupon.getMaxUsage())
                .currentUsage(coupon.getCurrentUsage())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .status(coupon.getStatus())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    private CouponUsageDto toUsageDto(CouponUsage usage) {
        return CouponUsageDto.builder()
                .id(usage.getId())
                .couponId(usage.getCouponId())
                .userId(usage.getUserId())
                .orderId(usage.getOrderId())
                .discountAmount(usage.getDiscountAmount())
                .usedAt(usage.getUsedAt())
                .build();
    }
}
