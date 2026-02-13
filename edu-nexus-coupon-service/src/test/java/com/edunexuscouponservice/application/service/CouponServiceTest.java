package com.edunexuscouponservice.application.service;

import com.edunexus.common.exception.BusinessException;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscouponservice.adapter.out.persistence.entity.Coupon;
import com.edunexuscouponservice.adapter.out.persistence.entity.CouponUsage;
import com.edunexuscouponservice.adapter.out.persistence.repository.CouponRepository;
import com.edunexuscouponservice.adapter.out.persistence.repository.CouponUsageRepository;
import com.edunexuscouponservice.domain.coupon.dto.*;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon testCoupon;
    private CreateCouponRequest createRequest;
    private ApplyCouponRequest applyRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        testCoupon = Coupon.builder()
                .id(1L)
                .code("TEST20")
                .type(CouponType.PERCENTAGE)
                .value(20.0)
                .minPurchaseAmount(100.0)
                .maxUsage(100)
                .currentUsage(0)
                .validFrom(now)
                .validUntil(tomorrow)
                .status(CouponStatus.ACTIVE)
                .build();

        createRequest = CreateCouponRequest.builder()
                .code("NEW20")
                .type(CouponType.PERCENTAGE)
                .value(20.0)
                .minPurchaseAmount(100.0)
                .maxUsage(100)
                .validFrom(now)
                .validUntil(tomorrow)
                .status(CouponStatus.ACTIVE)
                .build();

        applyRequest = ApplyCouponRequest.builder()
                .userId(1L)
                .orderAmount(150.0)
                .orderId(100L)
                .build();
    }

    @Test
    void createCoupon_ShouldReturnCouponDto() {
        when(couponRepository.findByCode("NEW20")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);

        CouponDto result = couponService.createCoupon(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(testCoupon.getCode());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void createCoupon_WithDuplicateCode_ShouldThrowException() {
        when(couponRepository.findByCode("NEW20")).thenReturn(Optional.of(testCoupon));

        assertThatThrownBy(() -> couponService.createCoupon(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createCoupon_WithInvalidDateRange_ShouldThrowException() {
        createRequest.setValidUntil(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> couponService.createCoupon(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("after valid from date");
    }

    @Test
    void createCoupon_WithInvalidPercentage_ShouldThrowException() {
        createRequest.setValue(150.0);

        assertThatThrownBy(() -> couponService.createCoupon(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    void getCouponByCode_WithValidCode_ShouldReturnCouponDto() {
        when(couponRepository.findByCode("TEST20")).thenReturn(Optional.of(testCoupon));

        CouponDto result = couponService.getCouponByCode("TEST20");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("TEST20");
    }

    @Test
    void getCouponByCode_WithInvalidCode_ShouldThrowException() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getCouponByCode("INVALID"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllActiveCoupons_ShouldReturnActiveCoupons() {
        Page<Coupon> couponPage = new PageImpl<>(Arrays.asList(testCoupon));
        when(couponRepository.findByStatus(eq(CouponStatus.ACTIVE), any()))
                .thenReturn(couponPage);

        List<CouponDto> result = couponService.getAllActiveCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CouponStatus.ACTIVE);
    }

    @Test
    void validateCoupon_WithValidCoupon_ShouldReturnValidResult() {
        when(couponRepository.findByCode("TEST20")).thenReturn(Optional.of(testCoupon));

        CouponValidationResult result = couponService.validateCoupon("TEST20", 150.0);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualTo(30.0);
    }

    @Test
    void validateCoupon_WithNonExistentCoupon_ShouldReturnInvalidResult() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        CouponValidationResult result = couponService.validateCoupon("INVALID", 150.0);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Coupon not found");
    }

    @Test
    void validateCoupon_WithInsufficientAmount_ShouldReturnInvalidResult() {
        when(couponRepository.findByCode("TEST20")).thenReturn(Optional.of(testCoupon));

        CouponValidationResult result = couponService.validateCoupon("TEST20", 50.0);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Minimum purchase amount");
    }

    @Test
    void applyCoupon_WithValidRequest_ShouldApplyCouponSuccessfully() {
        when(couponRepository.findByCode("TEST20")).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);
        when(couponUsageRepository.save(any(CouponUsage.class))).thenReturn(mock(CouponUsage.class));

        CouponValidationResult result = couponService.applyCoupon("TEST20", applyRequest);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualTo(30.0);
        verify(couponRepository).save(any(Coupon.class));
        verify(couponUsageRepository).save(any(CouponUsage.class));
    }

    @Test
    void applyCoupon_WithInvalidCoupon_ShouldReturnInvalidResult() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        CouponValidationResult result = couponService.applyCoupon("INVALID", applyRequest);

        assertThat(result.isValid()).isFalse();
        verify(couponRepository, never()).save(any());
        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void getUserCouponUsage_ShouldReturnUserUsages() {
        CouponUsage usage = CouponUsage.builder()
                .id(1L)
                .couponId(1L)
                .userId(1L)
                .orderId(100L)
                .discountAmount(30.0)
                .usedAt(LocalDateTime.now())
                .build();

        when(couponUsageRepository.findByUserId(1L)).thenReturn(Arrays.asList(usage));

        List<CouponUsageDto> result = couponService.getUserCouponUsage(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void updateCouponStatus_WithValidStatus_ShouldUpdateStatus() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);

        CouponDto result = couponService.updateCouponStatus(1L, "INACTIVE");

        assertThat(result.getStatus()).isEqualTo(CouponStatus.INACTIVE);
    }

    @Test
    void updateCouponStatus_WithInvalidStatus_ShouldThrowException() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        assertThatThrownBy(() -> couponService.updateCouponStatus(1L, "INVALID"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteCoupon_WithValidId_ShouldDeleteCoupon() {
        when(couponRepository.existsById(1L)).thenReturn(true);

        couponService.deleteCoupon(1L);

        verify(couponRepository).deleteById(1L);
    }

    @Test
    void deleteCoupon_WithInvalidId_ShouldThrowException() {
        when(couponRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> couponService.deleteCoupon(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void calculateDiscount_WithPercentageType_ShouldCalculateCorrectly() {
        Double discount = testCoupon.calculateDiscount(150.0);

        assertThat(discount).isEqualTo(30.0);
    }

    @Test
    void calculateDiscount_WithFixedAmountType_ShouldCalculateCorrectly() {
        testCoupon.setType(CouponType.FIXED_AMOUNT);
        testCoupon.setValue(25.0);

        Double discount = testCoupon.calculateDiscount(150.0);

        assertThat(discount).isEqualTo(25.0);
    }
}
