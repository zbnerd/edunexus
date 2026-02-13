package com.edunexuscouponservice.adapter.out.persistence.entity;

import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "coupons")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    private Double value;

    @Column(name = "min_purchase_amount")
    private Double minPurchaseAmount;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "current_usage")
    private Integer currentUsage;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CouponStatus.ACTIVE
                && now.isAfter(validFrom)
                && now.isBefore(validUntil)
                && (maxUsage == null || currentUsage < maxUsage);
    }

    public boolean canApplyToAmount(Double purchaseAmount) {
        if (minPurchaseAmount == null || minPurchaseAmount == 0) {
            return true;
        }
        return purchaseAmount != null && purchaseAmount >= minPurchaseAmount;
    }

    public Double calculateDiscount(Double originalAmount) {
        if (originalAmount == null || originalAmount <= 0) {
            return 0.0;
        }

        if (!canApplyToAmount(originalAmount)) {
            return 0.0;
        }

        return switch (type) {
            case PERCENTAGE -> originalAmount * (value / 100.0);
            case FIXED_AMOUNT -> Math.min(value, originalAmount);
        };
    }

    public void incrementUsage() {
        if (currentUsage == null) {
            currentUsage = 0;
        }
        currentUsage++;
    }

    public void markAsExpired() {
        this.status = CouponStatus.EXPIRED;
    }

    public void deactivate() {
        this.status = CouponStatus.INACTIVE;
    }

    public void activate() {
        this.status = CouponStatus.ACTIVE;
    }

    public void setStatus(CouponStatus status) {
        this.status = status;
    }
}
