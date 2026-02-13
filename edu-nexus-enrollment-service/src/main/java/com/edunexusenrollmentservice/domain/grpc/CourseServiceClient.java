package com.edunexusenrollmentservice.domain.grpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * gRPC client for communicating with Course Service.
 * Used by Saga orchestrator to validate courses and update capacity.
 *
 * Note: This is a placeholder implementation that returns default values.
 * In production, implement actual gRPC calls to the course service
 * or add the grpc-client-spring-boot-starter dependency.
 */
@Slf4j
@Service
public class CourseServiceClient {

    // Note: This is a placeholder implementation. In a real scenario,
    // you would have a CourseService gRPC definition in edu-nexus-grpc-common
    // and inject it using @GrpcClient annotation.

    /**
     * Validate that a course exists and is available for enrollment.
     *
     * @param courseId The course ID to validate
     * @return true if course is valid and available, false otherwise
     */
    public boolean validateCourse(Long courseId) {
        log.debug("Validating course: courseId={}", courseId);

        // TODO: Implement actual gRPC call to course service
        // For now, return true as a placeholder
        // In production:
        // return courseServiceStub.validateCourse(CourseValidationRequest.newBuilder()
        //     .setCourseId(courseId)
        //     .build()).getValid();

        return true;
    }

    /**
     * Update course capacity by the specified delta.
     *
     * @param courseId The course ID
     * @param delta The change in capacity (negative to decrement, positive to increment)
     * @return true if update was successful, false otherwise
     */
    public boolean updateCourseCapacity(Long courseId, int delta) {
        log.debug("Updating course capacity: courseId={}, delta={}", courseId, delta);

        // TODO: Implement actual gRPC call to course service
        // For now, return true as a placeholder
        // In production:
        // CourseCapacityUpdateResponse response = courseServiceStub.updateCapacity(
        //     CourseCapacityUpdateRequest.newBuilder()
        //         .setCourseId(courseId)
        //         .setDelta(delta)
        //         .build());
        // return response.getSuccess();

        return true;
    }
}
