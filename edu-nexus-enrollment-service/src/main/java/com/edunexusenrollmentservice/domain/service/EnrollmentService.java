package com.edunexusenrollmentservice.domain.service;

import com.edunexusenrollmentservice.domain.dto.EnrollmentDto;
import com.edunexusenrollmentservice.domain.dto.SubscriptionDto;
import com.edunexusenrollmentservice.domain.entity.Enrollment;
import com.edunexusenrollmentservice.domain.entity.Subscription;
import com.edunexusenrollmentservice.domain.repository.EnrollmentRepository;
import com.edunexusenrollmentservice.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Registers a user for a course.
     *
     * @param enrollmentDto containing userId, courseId, and paymentId
     * @return the created Enrollment entity
     * @throws IllegalArgumentException if enrollmentDto is null
     */
    @Transactional
    public Enrollment registerCourse(EnrollmentDto enrollmentDto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentInfo(enrollmentDto);
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Register a course enrollment with explicit parameters.
     * Used by Saga orchestrator for distributed transaction coordination.
     *
     * @param userId The user ID
     * @param courseId The course ID
     * @param paymentId The payment ID from previous step
     * @return The created enrollment
     */
    @Transactional
    public Enrollment registerCourse(Long userId, Long courseId, Long paymentId) {
        EnrollmentDto dto = EnrollmentDto.builder()
                .userId(userId)
                .courseId(courseId)
                .paymentId(paymentId)
                .build();
        return registerCourse(dto);
    }

    /**
     * Creates or manages a subscription plan.
     *
     * @param subscriptionDto containing subscription details
     * @return the created Subscription entity
     * @throws IllegalArgumentException if subscriptionDto is null
     */
    @Transactional
    public Subscription manageSubscription(SubscriptionDto subscriptionDto) {
        Subscription subscription = new Subscription();
        subscription.setSubscriptionInfo(subscriptionDto);
        return subscriptionRepository.save(subscription);
    }

    /**
     * Renews an existing subscription with new date range.
     *
     * @param subscriptionId the ID of the subscription to renew
     * @param newStartDate the new subscription start date
     * @param newEndDate the new subscription end date
     * @return the renewed Subscription entity
     * @throws IllegalStateException if subscription doesn't exist
     */
    @Transactional
    public Subscription renewSubscription(long subscriptionId, LocalDateTime newStartDate, LocalDateTime newEndDate) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found with id: " + subscriptionId));

        subscription.renewSubscription(newStartDate, newEndDate);
        return subscription;
    }

    /**
     * Checks if a user has access to a specific course.
     *
     * @param userId the user's unique identifier
     * @param courseId the course's unique identifier
     * @return true if user is enrolled in the course, false otherwise
     */
    public boolean checkCourseAccess(long userId, long courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        return enrollment.isPresent();
    }

    /**
     * Checks if a user has an active subscription at a given time.
     *
     * @param userId the user's unique identifier
     * @param now the timestamp to check subscription validity against
     * @return true if user has an active subscription, false otherwise
     */
    public boolean checkSubscriptionAccess(long userId, LocalDateTime now) {
        Optional<Subscription> subscription = subscriptionRepository.findTopByUserIdAndEndDateAfterOrderByEndDateDesc(userId, now);
        return subscription.isPresent() && !subscription.get().getEndDate().isBefore(now);
    }

    /**
     * Retrieves all course enrollments for a specific user.
     *
     * @param userId the user's unique identifier
     * @return list of all enrollments for the user
     */
    public List<Enrollment> getUserEnrollments(long userId) {
        Page<Enrollment> page = enrollmentRepository.findAllByUserId(userId, Pageable.unpaged());
        return page.getContent();
    }

    /**
     * Retrieves all subscription plans for a specific user.
     *
     * @param userId the user's unique identifier
     * @return list of all subscriptions for the user
     */
    public List<Subscription> getUserPlanSubscriptions(long userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }
}
