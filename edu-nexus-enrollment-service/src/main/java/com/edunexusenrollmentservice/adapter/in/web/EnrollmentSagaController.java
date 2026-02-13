package com.edunexusenrollmentservice.adapter.in.web;

import com.edunexusenrollmentservice.application.saga.coordinator.DistributedTransactionCoordinator;
import com.edunexusenrollmentservice.application.saga.orchestrator.EnrollmentSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for triggering and monitoring saga-based enrollment.
 * Provides endpoints for distributed transaction coordination.
 */
@Slf4j
@RestController
@RequestMapping("/api/enrollments/saga")
@RequiredArgsConstructor
public class EnrollmentSagaController {

    private final EnrollmentSagaOrchestrator sagaOrchestrator;
    private final DistributedTransactionCoordinator transactionCoordinator;

    /**
     * Enroll a user in a course using saga pattern.
     *
     * @param request containing userId and courseId
     * @return enrollment result with saga ID for tracking
     */
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollWithSaga(@RequestBody EnrollmentRequest request) {
        log.info("Received saga enrollment request: userId={}, courseId={}",
                request.userId(), request.courseId());

        EnrollmentSagaOrchestrator.SagaExecutionResult result =
                sagaOrchestrator.executeEnrollmentSaga(
                        request.userId(),
                        request.courseId()
                );

        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "sagaId", result.sagaId(),
                    "enrollmentId", result.enrollmentId(),
                    "paymentId", result.paymentId()
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "sagaId", result.sagaId(),
                    "error", result.message()
            ));
        }
    }

    /**
     * Get transaction state for a saga.
     *
     * @param sagaId The saga ID
     * @return transaction state
     */
    @GetMapping("/status/{sagaId}")
    public ResponseEntity<?> getTransactionStatus(@PathVariable String sagaId) {
        DistributedTransactionCoordinator.TransactionState state =
                transactionCoordinator.getTransactionState(sagaId);

        if (state == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "sagaId", state.getSagaId(),
                "userId", state.getUserId(),
                "courseId", state.getCourseId(),
                "status", state.getStatus(),
                "errorMessage", state.getErrorMessage(),
                "completedSteps", state.getCompletedSteps()
        ));
    }

    /**
     * Request body for enrollment request.
     */
    public record EnrollmentRequest(
            Long userId,
            Long courseId
    ) {}
}
