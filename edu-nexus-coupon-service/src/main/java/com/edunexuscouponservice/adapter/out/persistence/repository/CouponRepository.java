package com.edunexuscouponservice.adapter.out.persistence.repository;

import com.edunexuscouponservice.adapter.out.persistence.entity.Coupon;
import com.edunexuscouponservice.domain.coupon.enums.CouponStatus;
import com.edunexuscouponservice.domain.coupon.enums.CouponType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    Optional<Coupon> findByCode(String code);

    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100")
    })
    Page<Coupon> findByStatus(CouponStatus status, Pageable pageable);

    List<Coupon> findAllByStatus(CouponStatus status);

    @Query("SELECT c FROM Coupon c WHERE c.status = :status " +
           "AND c.validFrom <= :now AND c.validUntil > :now")
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100")
    })
    Page<Coupon> findActiveCoupons(@Param("status") CouponStatus status,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.status = :status " +
           "AND c.validFrom <= :now AND c.validUntil > :now")
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Coupon> findAllActiveCoupons(@Param("status") CouponStatus status,
                                      @Param("now") LocalDateTime now);

    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validFrom <= :now AND c.validUntil > :now " +
           "AND (c.maxUsage IS NULL OR c.currentUsage < c.maxUsage)")
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100")
    })
    Page<Coupon> findValidCoupons(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.validFrom <= :now AND c.validUntil > :now " +
           "AND (c.maxUsage IS NULL OR c.currentUsage < c.maxUsage)")
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Coupon> findAllValidCoupons(@Param("now") LocalDateTime now);

    @Query("SELECT cu FROM CouponUsage cu WHERE cu.userId = :userId " +
           "ORDER BY cu.usedAt DESC")
    Page<com.edunexuscouponservice.adapter.out.persistence.entity.CouponUsage> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT cu FROM CouponUsage cu WHERE cu.userId = :userId " +
           "ORDER BY cu.usedAt DESC")
    List<com.edunexuscouponservice.adapter.out.persistence.entity.CouponUsage> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.couponId = :couponId")
    Long countUsageByCouponId(@Param("couponId") Long couponId);
}
