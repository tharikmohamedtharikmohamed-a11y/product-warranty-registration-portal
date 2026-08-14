package com.warrantyportal.exception;

public class MaxFileSizeExceededException extends RuntimeException {
    public MaxFileSizeExceededException(String message) {
        super(message);
    }
}
