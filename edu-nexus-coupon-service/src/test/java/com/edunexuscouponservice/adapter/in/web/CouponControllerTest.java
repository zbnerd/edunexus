package com.edunexuscouponservice.adapter.in.web;

import com.edunexuscouponservice.adapter.in.web.request.CouponApplyRequest;
import com.edunexuscouponservice.adapter.in.web.request.CouponCreateRequest;
import com.edunexuscouponservice.domain.coupon.dto.*;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import com.edunexuscouponservice.port.in.CouponUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponUseCase couponUseCase;

    @Test
    void createCoupon_ShouldReturnCreatedCoupon() throws Exception {
        CouponCreateRequest request = new CouponCreateRequest();
        request.setCode("TEST20");
        request.setType(CouponType.PERCENTAGE);
        request.setValue(20.0);
        request.setMinPurchaseAmount(100.0);
        request.setMaxUsage(100);
        request.setValidFrom(LocalDateTime.now());
        request.setValidUntil(LocalDateTime.now().plusDays(30));
        request.setStatus(CouponStatus.ACTIVE);

        CouponDto couponDto = CouponDto.builder()
                .id(1L)
                .code("TEST20")
                .type(CouponType.PERCENTAGE)
                .value(20.0)
                .minPurchaseAmount(100.0)
                .maxUsage(100)
                .currentUsage(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .status(CouponStatus.ACTIVE)
                .build();

        when(couponUseCase.createCoupon(any())).thenReturn(couponDto);

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST20"))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.value").value(20.0));
    }

    @Test
    void getAllCoupons_ShouldReturnListOfCoupons() throws Exception {
        CouponDto couponDto = CouponDto.builder()
                .id(1L)
                .code("TEST20")
                .type(CouponType.PERCENTAGE)
                .value(20.0)
                .status(CouponStatus.ACTIVE)
                .build();

        when(couponUseCase.getAllCoupons()).thenReturn(Arrays.asList(couponDto));

        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("TEST20"));
    }

    @Test
    void getCouponByCode_ShouldReturnCoupon() throws Exception {
        CouponDto couponDto = CouponDto.builder()
                .id(1L)
                .code("TEST20")
                .type(CouponType.PERCENTAGE)
                .value(20.0)
                .status(CouponStatus.ACTIVE)
                .build();

        when(couponUseCase.getCouponByCode("TEST20")).thenReturn(couponDto);

        mockMvc.perform(get("/coupons/TEST20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST20"));
    }

    @Test
    void validateCoupon_ShouldReturnValidationResult() throws Exception {
        CouponValidationResult result = CouponValidationResult.valid(30.0);

        when(couponUseCase.validateCoupon(eq("TEST20"), eq(150.0)))
                .thenReturn(result);

        mockMvc.perform(post("/coupons/TEST20/validate")
                        .param("orderAmount", "150.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(30.0));
    }

    @Test
    void applyCoupon_ShouldApplyCouponSuccessfully() throws Exception {
        CouponApplyRequest request = new CouponApplyRequest();
        request.setUserId(1L);
        request.setOrderAmount(150.0);
        request.setOrderId(100L);

        CouponValidationResult result = CouponValidationResult.valid(30.0);

        when(couponUseCase.applyCoupon(eq("TEST20"), any()))
                .thenReturn(result);

        mockMvc.perform(post("/coupons/TEST20/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(30.0));
    }

    @Test
    void getUserCouponUsage_ShouldReturnUserUsages() throws Exception {
        CouponUsageDto usageDto = CouponUsageDto.builder()
                .id(1L)
                .couponId(1L)
                .userId(1L)
                .orderId(100L)
                .discountAmount(30.0)
                .usedAt(LocalDateTime.now())
                .build();

        when(couponUseCase.getUserCouponUsage(1L)).thenReturn(Arrays.asList(usageDto));

        mockMvc.perform(get("/coupons/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    void updateCouponStatus_ShouldUpdateStatus() throws Exception {
        CouponDto couponDto = CouponDto.builder()
                .id(1L)
                .code("TEST20")
                .status(CouponStatus.INACTIVE)
                .build();

        when(couponUseCase.updateCouponStatus(eq(1L), eq("INACTIVE")))
                .thenReturn(couponDto);

        mockMvc.perform(patch("/coupons/1/status")
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void deleteCoupon_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/coupons/1"))
                .andExpect(status().isNoContent());
    }
}
