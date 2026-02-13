package com.edunexusenrollmentservice.domain.repository;

import com.edunexusenrollmentservice.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.payment WHERE s.userId = :userId AND s.endDate > :endDate ORDER BY s.endDate DESC")
    Optional<Subscription> findTopByUserIdAndEndDateAfterOrderByEndDateDesc(@Param("userId") Long userId, @Param("endDate") LocalDateTime endDate);

    List<Subscription> findAllByUserId(Long userId);
}