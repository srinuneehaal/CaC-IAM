package com.cac.iam.exception;

/**
 * Thrown when no parsing strategy exists for a requested file category.
 */
public class UnsupportedFileCategoryException extends RuntimeException {

    /**
     * Creates an exception for an unsupported file category.
     *
     * @param message description of the unsupported category
     */
    public UnsupportedFileCategoryException(String message) {
        super(message);
    }
}
