package com.cac.iam.exception;

/**
 * Thrown when no applier exists for a given file category.
 */
public class MissingApplierException extends PlanApplyException {

    /**
     * Creates an exception for a missing applier.
     *
     * @param message description of the missing applier
     */
    public MissingApplierException(String message) {
        super(message);
    }
}
