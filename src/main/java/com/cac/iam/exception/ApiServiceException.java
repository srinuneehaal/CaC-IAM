package com.cac.iam.exception;

/**
 * Thrown when an API service operation fails.
 */
public class ApiServiceException extends PlanApplyException {

    /**
     * Creates an exception with only a message.
     *
     * @param message error description
     */
    public ApiServiceException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and root cause.
     *
     * @param message error description
     * @param cause   underlying cause
     */
    public ApiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
