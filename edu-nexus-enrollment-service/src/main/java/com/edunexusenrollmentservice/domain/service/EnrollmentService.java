package com.edunexusenrollmentservice.domain.service;

import com.edunexusenrollmentservice.domain.dto.EnrollmentDto;
import com.edunexusenrollmentservice.domain.dto.SubscriptionDto;
import com.edunexusenrollmentservice.domain.entity.Enrollment;
import com.edunexusenrollmentservice.domain.entity.Subscription;
import com.edunexusenrollmentservice.domain.repository.EnrollmentRepository;
import com.edunexusenrollmentservice.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public Enrollment registerCourse(EnrollmentDto enrollmentDto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentInfo(enrollmentDto);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Subscription manageSubscription(SubscriptionDto subscriptionDto) {
        Subscription subscription = new Subscription();
        subscription.setSubscriptionInfo(subscriptionDto);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription renewSubscription(long subscriptionId, LocalDateTime newStartDate, LocalDateTime newEndDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found with id: " + subscriptionId));

        subscription.renewSubscription(newStartDate, newEndDate);
        return subscription;
    }

    public boolean checkCourseAccess(long userId, long courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        return enrollment.isPresent();
    }

    public boolean checkSubscriptionAccess(long userId, LocalDateTime now) {
        Optional<Subscription> subscription = subscriptionRepository.findTopByUserIdAndEndDateAfterOrderByEndDateDesc(userId, now);
        return subscription.isPresent() && !subscription.get().getEndDate().isBefore(now);
    }

    public List<Enrollment> getUserEnrollments(long userId) {
        return enrollmentRepository.findAllByUserId(userId);
    }

    public List<Subscription> getUserPlanSubscriptions(long userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }
}
