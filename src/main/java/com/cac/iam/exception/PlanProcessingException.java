package com.cac.iam.exception;

/**
 * Generic wrapper for errors encountered while building or processing plans.
 */
public class PlanProcessingException extends RuntimeException {

    /**
     * Creates an exception with a message only.
     *
     * @param message error description
     */
    public PlanProcessingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and root cause.
     *
     * @param message error description
     * @param cause   underlying cause
     */
    public PlanProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
