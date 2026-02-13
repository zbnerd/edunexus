package com.edunexusenrollmentservice.application.saga.step;

/**
 * Represents a single step in the saga orchestration.
 * Each step has an execute action and a compensate action for rollback.
 */
public interface SagaStep {

    /**
     * Execute the saga step.
     *
     * @param context The saga context containing all necessary data
     * @return StepExecutionResult indicating success or failure
     * @throws Exception if execution fails catastrophically
     */
    StepExecutionResult execute(SagaContext context) throws Exception;

    /**
     * Compensate/rollback the saga step.
     * Called when a subsequent step fails.
     *
     * @param context The saga context containing all necessary data
     */
    void compensate(SagaContext context);

    /**
     * Get the step name for logging and tracking.
     *
     * @return The step name
     */
    String getStepName();

    /**
     * Result of executing a saga step.
     */
    record StepExecutionResult(
            boolean success,
            String message,
            Object data  // Any data produced by this step for subsequent steps
    ) {
        public static StepExecutionResult success(String message) {
            return new StepExecutionResult(true, message, null);
        }

        public static StepExecutionResult success(String message, Object data) {
            return new StepExecutionResult(true, message, data);
        }

        public static StepExecutionResult failure(String message) {
            return new StepExecutionResult(false, message, null);
        }
    }
}
