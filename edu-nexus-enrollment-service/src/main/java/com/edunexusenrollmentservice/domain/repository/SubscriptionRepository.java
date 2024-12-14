package com.edunexusenrollmentservice.domain.repository;

import com.edunexusenrollmentservice.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByUserIdAndEndDateAfterOrderByEndDateDesc(Long userId, LocalDateTime endDate);
    List<Subscription> findAllByUserId(Long userId);
}