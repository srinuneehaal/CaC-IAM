package com.cac.iam.exception;

/**
 * Thrown when no parsing strategy supports a given path.
 */
public class UnsupportedFilePathException extends RuntimeException {

    /**
     * Creates an exception for an unsupported file path.
     *
     * @param message description of the unsupported path
     */
    public UnsupportedFilePathException(String message) {
        super(message);
    }
}
