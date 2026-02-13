package com.edunexuscouponservice.adapter.out.persistence.repository;

import com.edunexuscouponservice.adapter.out.persistence.entity.Coupon;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByStatus(CouponStatus status);

    @Query("SELECT c FROM Coupon c WHERE c.status = :status " +
           "AND c.validFrom <= :now AND c.validUntil > :now")
    List<Coupon> findActiveCoupons(@Param("status") CouponStatus status,
                                   @Param("now") LocalDateTime now);

    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validFrom <= :now AND c.validUntil > :now " +
           "AND (c.maxUsage IS NULL OR c.currentUsage < c.maxUsage)")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);

    @Query("SELECT cu FROM CouponUsage cu WHERE cu.userId = :userId " +
           "ORDER BY cu.usedAt DESC")
    List<com.edunexuscouponservice.adapter.out.persistence.entity.CouponUsage> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.couponId = :couponId")
    Long countUsageByCouponId(@Param("couponId") Long couponId);
}
