package com.cac.iam.exception;

/**
 * Base exception for failures encountered while applying a plan.
 */
public class PlanApplyException extends RuntimeException {

    /**
     * Creates an exception with only a message.
     *
     * @param message error description
     */
    public PlanApplyException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and root cause.
     *
     * @param message error description
     * @param cause   underlying cause
     */
    public PlanApplyException(String message, Throwable cause) {
        super(message, cause);
    }
}
