package com.edunexuscouponservice.adapter.out.persistence.repository;

import com.edunexuscouponservice.adapter.out.persistence.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    List<CouponUsage> findByUserId(Long userId);

    List<CouponUsage> findByCouponId(Long couponId);

    @Query("SELECT cu FROM CouponUsage cu WHERE cu.userId = :userId " +
           "AND cu.couponId = :couponId")
    List<CouponUsage> findByUserIdAndCouponId(@Param("userId") Long userId,
                                               @Param("couponId") Long couponId);

    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.couponId = :couponId")
    Long countByCouponId(@Param("couponId") Long couponId);

    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.userId = :userId " +
           "AND cu.couponId = :couponId")
    Long countByUserIdAndCouponId(@Param("userId") Long userId,
                                   @Param("couponId") Long couponId);
}
