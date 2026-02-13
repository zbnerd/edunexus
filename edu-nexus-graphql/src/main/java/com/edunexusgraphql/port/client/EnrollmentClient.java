package com.edunexusgraphql.port.client;

import com.edunexusgraphql.model.Enrollment;
import com.edunexusgraphql.model.Payment;
import com.edunexusgraphql.model.PlanSubscription;

import java.util.List;
import java.util.Map;

/**
 * Client interface for enrollment service operations.
 * Abstracts gRPC concerns from the application layer.
 */
public interface EnrollmentClient {

    /**
     * Check if a user has access to a specific course.
     *
     * @param courseId Course ID
     * @param userId User ID
     * @return true if user has access, false otherwise
     */
    boolean checkCourseAccess(long courseId, long userId);

    /**
     * Check if a user has an active subscription.
     *
     * @param userId User ID
     * @return true if user has subscription access, false otherwise
     */
    boolean checkSubscriptionAccess(long userId);

    /**
     * Get all enrollments for a user.
     *
     * @param userId User ID
     * @return List of enrollments
     */
    List<Enrollment> getEnrollmentsByUserId(Long userId);

    /**
     * Get all subscriptions for a user.
     *
     * @param userId User ID
     * @return List of subscriptions
     */
    List<PlanSubscription> getSubscriptionsByUserId(Long userId);

    /**
     * Find a payment by ID.
     *
     * @param paymentId Payment ID
     * @return Payment details
     */
    Payment findPaymentById(Long paymentId);

    /**
     * Find multiple payments by their IDs.
     *
     * @param paymentIds List of payment IDs
     * @return Map of payment ID to Payment details
     */
    Map<Long, Payment> findPaymentsByIds(List<Long> paymentIds);
}
